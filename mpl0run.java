import java.io.*;
import java.nio.file.Paths;
import java.nio.file.Files;

class mpl0run {

  public mpl0run(String filename) {
    System.out.printf("mpl0run\n");
    test(filename);
  }

  String mpl0source = "";

  static final int T_PSH=0, T_PRF=6, T_END=7, T_STO=8, T_JMP=11, T_FNC=12, T_POP=14, T_BEQ=15, T_PRINT=16;
  static final int TM_MEM=1, TM_MEMIND=1, TM_INT=5, TM_IMM_I=3, TM_IMM_D=4, TM_PC=2;
  static final int O_LT=0, O_PLUS=1, O_MULT=2, O_MINUS=3, O_DIV=4;
  static final int F_cos=0, F_sin=1, F_int=2;

    int MAXPROG=10000;
    int MAXMEM=3000;
    int MAXSTK=100;
    int numobj=1000;
    int prog[]=new int[MAXPROG];  int pp=0;
    double progparam_d[]=new double[MAXPROG];
    int progparam_i[]=new int[MAXPROG];
    int progparam_mem[]=new int[MAXPROG]; 
    double mem[]=new double[MAXMEM];    int mp=0;
    int memi[]=new int[MAXMEM];   
    double stk[]=new double[MAXSTK]; int sp=0;
// only for parsing
    String progparam_str[]=new String[MAXPROG]; 
    String mem_str[]=new String[MAXMEM]; 
    
    static final int M_NONE=0, M_VAR=1, M_NUM=2, M_STORE=3, M_LABEL=4, M_ADDR=5, M_OP=6, M_READADDR=7, M_JUMP=8, M_BRANCH=9, M_STOADDR=10, M_PRINT=11, M_END=12, M_MEMORY=13,M_E=14,M_POP=15;
    int f=M_NONE;
    int m=M_NONE;
    String build="";
    char op=' ';
  boolean writeprog=true;
  
  int parse_mem(String s) throws NoSuchFieldException {
    //try {
      //return strip_m(s,1); // old way - but breaks m1 type variables
    //} catch (java.lang.NumberFormatException e) {
      // search for it in memory list
      int i;
      for (i=0; i<mp; ++i) if (s.equals(mem_str[i])) { 
        //System.out.printf("FOund it %s=%d\n",s,i);
        return i;
      }
      //throw java.lang.NumberFormatException;
      System.out.printf("*** Could not find %s\n",s);
      throw new NoSuchFieldException();
      //return -1;
    //}
  }
  int strip_m(String s, int start) {
    int x;
    x=Integer.parseInt(s.substring(start));
    //System.out.printf("x=%d\n",x);
    return x;
  }

  double strip_double(String s, int start) {
    double x;
    x=Double.parseDouble(s.substring(start));
    return x;
  }

  String labelstr[]=new String[100];
  int labelint[]=new int[100];
  int labeltop=0;

  void add_label(String label, int pp) {
    labelstr[labeltop]=label;
    labelint[labeltop]=pp;
    labeltop++;
  }
  void printinstr() throws NoSuchFieldException {
        if (f==M_MEMORY && m==M_NUM) {
          System.out.printf("            .MEMORY %s = %dd\n",keep_label,strip_m(build,0));
          if (writeprog) { mem_str[mp]=keep_label; mp+=strip_m(build,0); }
        } else if (f==M_OP) {
            System.out.printf("   %-8s : PRF %c\n","",op);
            if (writeprog && op=='<') { prog[pp]=T_PRF; progparam_mem[pp]=O_LT; pp++; }
            if (writeprog && op=='+') { prog[pp]=T_PRF; progparam_mem[pp]=O_PLUS; pp++; }
            if (writeprog && op=='-') { prog[pp]=T_PRF; progparam_mem[pp]=O_MINUS; pp++; }
            if (writeprog && op=='*') { prog[pp]=T_PRF; progparam_mem[pp]=O_MULT; pp++; }
            if (writeprog && op=='/') { prog[pp]=T_PRF; progparam_mem[pp]=O_DIV; pp++; }
        } else if (f==M_POP) {
            System.out.printf("   %-8s : POP\n","");
            if (writeprog) { prog[pp]=T_POP; pp++; }
        } else if (f==M_END) {
            System.out.printf("   %-8s : END\n","");
            if (writeprog) { prog[pp]=T_END; pp++; }
        } else if (f==M_PRINT) {
            System.out.printf("   %-8s : PRINT %s\n","",build);
            if (writeprog) { prog[pp]=T_PRINT; progparam_mem[pp]=parse_mem(build); pp++; }
        } else if (f==M_STOADDR) {
            System.out.printf("   %-8s : STO []\n","");
            if (writeprog) { prog[pp]=T_STO | TM_MEMIND ; pp++; }
        } else if (f==M_READADDR) {
            System.out.printf("   %-8s : FNC []\n","");
            if (writeprog) { prog[pp]=T_FNC | TM_MEMIND ; pp++; }
        } else if (m==M_NUM || m==M_VAR) {
          //System.out.printf("             got %s %d %d op=%c\n",build,m,f,op);
          if (f==M_LABEL) {
            System.out.printf("  :%-8s : LABEL\n",build);
            if (writeprog) {
              // keep a copy of pp for this label
              add_label(build,pp);
              // at the end, rewrite all the jumps and beqs
              progparam_str[pp]=new String(build); // for reconstruction
            }
          } else if (m==M_NUM && f==M_NONE && false) {
            System.out.printf("   %-8s : PSH# %s\n","",build);
            if (writeprog) { prog[pp]=T_PSH|TM_IMM_I; progparam_i[pp]=strip_m(build,0); pp++; }
          } else if (m==M_NUM && f==M_NONE) {
            System.out.printf("   %-8s : PSH#D %s\n","",build);
            if (writeprog) { prog[pp]=T_PSH|TM_IMM_D; progparam_d[pp]=strip_double(build,0); pp++; }
          } else if (m==M_VAR && f==M_JUMP) {
            System.out.printf("   %-8s : JMP %s\n","",build);
            //if (writeprog) { prog[pp]=T_JMP; progparam_mem[pp]=strip_m(build,1); pp++; }
            if (writeprog) { prog[pp]=T_JMP; progparam_mem[pp]=-1; progparam_str[pp]=new String(build); pp++; }
          } else if (m==M_NUM && f==M_BRANCH) {
            System.out.printf("   %-8s : BEQ %+d\n","",strip_m(build,0));
            //if (writeprog) { prog[pp]=T_BEQ; progparam_mem[pp]=strip_m(build,1); pp++; }
            if (writeprog) { prog[pp]=T_BEQ; progparam_mem[pp]=pp+strip_m(build,0); pp++; }
          } else if (m==M_VAR && f==M_BRANCH) {
            System.out.printf("   %-8s : BEQ %s\n","",build);
            //if (writeprog) { prog[pp]=T_BEQ; progparam_mem[pp]=strip_m(build,1); pp++; }
            if (writeprog) { prog[pp]=T_BEQ; progparam_mem[pp]=-1; progparam_str[pp]=new String(build); pp++; }
          } else if (m==M_VAR && f==M_ADDR) {
            System.out.printf("   %-8s : PSH [%s]\n","",build);
            if (writeprog) { prog[pp]=T_PSH | TM_MEM ; progparam_mem[pp]=parse_mem(build); pp++; }
          } else if (m==M_VAR && f==M_STORE) {
            System.out.printf("   %-8s : STO %s\n","",build);
            if (writeprog) { prog[pp]=T_STO | (parse_mem(build)==0?TM_PC:0); progparam_mem[pp]=parse_mem(build); pp++; }
          } else if (m==M_VAR && f==M_NONE) {
            // check for functions
            if (build.equals("sin")) {
              System.out.printf("   %-8s : FNC %s\n","",build);
              if (writeprog) { prog[pp]=T_FNC; progparam_mem[pp]=F_sin; pp++; }
            } else if (build.equals("cos")) {
              System.out.printf("   %-8s : FNC %s\n","",build);
              if (writeprog) { prog[pp]=T_FNC; progparam_mem[pp]=F_cos; pp++; }
            } else if (build.equals("int")) {
              System.out.printf("   %-8s : FNC %s\n","",build);
              if (writeprog) { prog[pp]=T_FNC; progparam_mem[pp]=F_int; pp++; }
            } else {
              System.out.printf("   %-8s : PSH %s\n","",build);
              if (writeprog) { prog[pp]=T_PSH | (parse_mem(build)==0?TM_PC:0) ; progparam_mem[pp]=parse_mem(build); pp++; }
            }
          }
        }
        m=M_NONE;
        f=M_NONE;
        op=' ';
        build="";
  }

  String keep_label="";

  void mpl0compile(String s, boolean rewriteprog) {
    try {
    writeprog=rewriteprog;
        if (writeprog) { 
           pp=0;
           mp=0;
        }
        // if implicit PC in memory 0
        if (writeprog) { mem_str[mp]="PC"; mp++; }
    for(char c : s.toCharArray()) {
      // process c
      if (c==' ' || c=='\n' || c=='\r' || c==',') {
        printinstr();
        continue;
      }
      if (c=='+' || c=='-' || c=='*' || c=='/' || c=='<') {
        printinstr();
        f=M_OP;
        op=c;
        printinstr();
        continue;
      }
      if (c=='^') {
   System.out.printf("***********got HAT ^\n");
        build="PC";
        m=M_VAR;
        printinstr();
        continue;
      }
      if (c=='@') {
        printinstr();
        f=M_READADDR;
        printinstr();
        continue;
      }
      if (c=='.' && f==M_STORE) {
   System.out.printf("***********got double dot\n");
        //build="PC";
        f=M_POP;
        printinstr();
        continue;
      }
      if (c=='.' && m!=M_NUM) {
        printinstr();
        f=M_STORE;
        continue;
      }
      if (c=='?') {
        printinstr();
        f=M_BRANCH;
        continue;
      }
      if (c=='=' && f==M_LABEL) {
        f=M_MEMORY;
        keep_label=new String(build);
        build="";
        m=M_NONE; // ready for a num
        continue;
      }
      if (c=='~') {
        f=M_PRINT;
        printinstr();
        continue;
      }
      if (c=='&') {
        f=M_STOADDR;
        printinstr();
        continue;
      }
      if (c==':') {
        printinstr();
        f=M_LABEL;
        continue;
      }
      if (f==M_JUMP && c=='#') {
        f=M_END;
        printinstr();
        continue;
      }
      if (c=='#') {
        printinstr();
        f=M_JUMP;
        continue;
      }
      if (c=='\'') {
        f=M_ADDR;
        printinstr();
        continue;
      }
      //System.out.printf("%c\n",c);
      if (m==M_NONE && c>='0' && c<='9') {
        m=M_NUM;
        build+=c+"";
        continue;
      }
      if (m==M_NONE && (c>='a' && c<='z' || c>='A' && c<='Z')) {
        m=M_VAR;
        build+=c+"";
        continue;
      }
      if (m==M_NUM && (c=='e' || c=='E')) {
        f=M_E;
        build+=c+"";
        continue;
      }
      if (m==M_NUM && (c=='.' || c>='0' && c<='9' || f==M_E && (c=='-' || c=='+')) 
          || m==M_VAR && (c>='a' && c<='z' || c>='A' && c<='Z' || c>='0' && c<='9' || c=='_')) {
        build+=c+"";
        continue;
      }
    }
    if (writeprog) {
      int i;
      int p;
      // perform label substitions
      for (p=0; p<pp; ++p) {
        if ((prog[p]==T_JMP || prog[p]==T_BEQ) && progparam_mem[p]==-1 ) {
          for (i=0; i<labeltop; ++i) {
            if (labelstr[i].equals(progparam_str[p])) {
              //System.out.printf("Found label %s at location %d\n",labelstr[i],labelint[i]);
              progparam_mem[p]=labelint[i];
            }
          }
        }
      }
      // if no end, manually add one
      if (prog[pp-1]!=T_END) {
        prog[pp++]=T_END;
      }
    }
    } catch (NoSuchFieldException e) {
      System.err.printf("Compile failed : Exception %s\n",e);
      pp=0; prog[pp]=T_END;
    }
  }

static String readFile(String path/*, Charset encoding*/) 
  throws java.io.IOException 
{
  byte[] encoded = Files.readAllBytes(Paths.get(path));
  return new String(encoded/*, encoding*/);
}

  void test(String filename) {

    try {
      mpl0source=readFile(filename);
    } catch (Exception e) {
      return;
    }
    System.out.printf("Compiling mpl0 source...\n");
    System.out.printf("--dissassembly..----------------------\n");
    mpl0compile(mpl0source, true);
		System.out.printf("Program steps %d\n",pp);
    System.out.printf("\n--reverse-compile-----------------------\n");
    mpl0reversecompile(0);
    System.out.printf("-----------------------------------------\n");
    // compile - show dissassembly - but dont rewrite prog
    // mpl0compile(assem,false);
    // System.out.printf("\n-------------------------\n");

    run_prog(0);

   System.out.printf("Total instructions = %d (if enabled)\n",ic);
    
  }

  int verbose=0;
  int ic=0;

  int run_prog(int pp) {
    //int instr;

    mainloop:
    while(true) {
      switch (prog[pp]) {
        case T_PSH|TM_IMM_D:  stk[sp++]=progparam_d[pp++];          continue mainloop;
        case T_PSH:           stk[sp++]=mem[progparam_mem[pp++]];   continue mainloop;
        case T_PSH|TM_INT:    stk[sp++]=memi[progparam_mem[pp++]];  continue mainloop;
        case T_PSH|TM_IMM_I:  stk[sp++]=progparam_i[pp++];          continue mainloop;
        case T_PSH|TM_MEM:    stk[sp++]=progparam_mem[pp++];        continue mainloop;
        case T_FNC|TM_MEMIND: stk[sp-1]=mem[(int)stk[sp-1]];        break;
        case T_FNC:
          switch (progparam_mem[pp++]) {
            case F_sin: stk[sp-1]=Math.sin(stk[sp-1]);              break;
            case F_cos: stk[sp-1]=Math.cos(stk[sp-1]);              break;
            case F_int: stk[sp-1]=(int)(stk[sp-1]);                 break;
          }
          continue mainloop;
        case T_PRF:
          switch (progparam_mem[pp++]) {
            case O_LT:    stk[sp-2]=(stk[sp-2]<stk[--sp])?-1:0;     break;
            case O_PLUS:  stk[sp-2]+=stk[--sp];                     break;
            case O_MINUS: stk[sp-2]-=stk[--sp];                     break;
            case O_MULT:  stk[sp-2]*=stk[--sp];                     break;
            case O_DIV:   stk[sp-2]/=stk[--sp];                     break;
          }
          continue mainloop;
        case T_STO:             mem[progparam_mem[pp++]]=stk[--sp]; continue mainloop;
        case T_STO | TM_MEMIND: mem[(int)stk[--sp]]=stk[--sp];      break;
        case T_STO | TM_PC:     pp=(int)stk[--sp]+1;                continue mainloop;
        case T_PSH | TM_PC:     stk[sp++]=pp++;                     continue mainloop;
        case T_BEQ:             if (stk[--sp]==0) { pp=progparam_mem[pp]; continue mainloop; } 
                                break;
        case T_JMP:             pp=progparam_mem[pp];               continue mainloop;
        case T_POP:             sp--;                               break;
        case T_PRINT:           System.out.printf("%.14f\n",mem[progparam_mem[pp]]); break;
        case T_END:
          return 0;
      }
      pp++;
    } /* mainloop */
  }

  // exactly from run_prog (was print_prog)
  int mpl0reversecompile(int pp) {
    //int instr;
    boolean nc=false;

    mainloop:
    while(true) {
      int i;
      for (i=0; prog[i]!=T_END ;++i) {
        switch (prog[i]) {
        case T_BEQ:
        case T_JMP:
          if (pp==progparam_mem[i]) {
            //System.out.printf("\n:(%d) ",pp);
            if (progparam_str[pp] != null) {
              System.out.printf("\n:%s ",progparam_str[pp]);
            } else {
              System.out.printf("\n:L%d ",pp);
            } 
            nc=false;
          }
          break;
        }
      }
      //instr=prog[pp];
      //ic++;
      switch (prog[pp]) {
        case T_PSH|TM_MEM:
          if (nc) System.out.printf(",");
          if (mem_str[progparam_mem[pp]] != null) {
            System.out.printf("%s'",mem_str[progparam_mem[pp]]);
          } else {
            System.out.printf("m%d'",progparam_mem[pp]);
          }
          nc=false;
				  break;
        case T_FNC|TM_MEMIND:
          System.out.printf("@",progparam_mem[pp]);
          nc=false;
				  break;
        case T_PSH|TM_INT:
          if (nc) System.out.printf(",");
          System.out.printf("i%d%",progparam_mem[pp]);
          nc=true;
				  break;
        case T_PSH|TM_IMM_I:
          if (nc) System.out.printf(",");
          System.out.printf("%d",progparam_i[pp]);
          nc=true;
          break;
        case T_PSH|TM_IMM_D:
          if (nc) System.out.printf(",");
          System.out.printf("%f",progparam_d[pp]);
          nc=true;
          break;
        case T_PRINT:
          if (nc) System.out.printf(",");
          if (mem_str[progparam_mem[pp]] != null) {
            System.out.printf("%s~",mem_str[progparam_mem[pp]]);
          } else {
            System.out.printf("m%d~",progparam_mem[pp]);
          }
          nc=false;
          break;
        case T_PSH:
          if (nc) System.out.printf(",");
          if (mem_str[progparam_mem[pp]] != null) {
            System.out.printf("%s",mem_str[progparam_mem[pp]]);
          } else {
            System.out.printf("m%d",progparam_mem[pp]);
          }
          nc=true;
          break;
        case T_FNC:
          if (nc) System.out.printf(",");
          switch (progparam_mem[pp]) {
            case F_sin:
              System.out.printf("sin");
              break;
            case F_cos:
              System.out.printf("cos");
              break;
            case F_int:
              System.out.printf("int");
              break;
          }
          nc=true;
          break;
        case T_PRF:
             //static final int O_LT=0, O_PLUS=1, O_MULT=2;
          if (verbose>0) { System.out.printf("  Performing op %d\n",progparam_mem[pp]); }
          switch (progparam_mem[pp]) {
            case O_LT:
              System.out.printf("<");
              break;
            case O_PLUS:
              System.out.printf("+");
              break;
            case O_MINUS:
              System.out.printf("-");
              break;
            case O_MULT:
              System.out.printf("*");
              break;
            case O_DIV:
              System.out.printf("/");
              break;
          }
          nc=false;
          break;
        case T_BEQ:
          //System.out.printf("?(%d)",progparam_mem[pp]);
          if (progparam_str[progparam_mem[pp]] != null) {
            System.out.printf("?%s",progparam_str[progparam_mem[pp]]);
          } else {
            System.out.printf("?L%d",progparam_mem[pp]);
          }
          nc=true;
          break;
        case T_JMP:
          //System.out.printf("#(%d)",progparam_mem[pp]);
          if (progparam_str[progparam_mem[pp]] != null) {
            System.out.printf("#%s",progparam_str[progparam_mem[pp]]);
          } else {
            System.out.printf("#L%d",progparam_mem[pp]);
          }
          nc=true;
          break;
        case T_STO | TM_MEMIND:
          System.out.printf(".&\n");
          nc=false;
          break;
        case T_STO:
          if (nc) System.out.printf(" ");
          if (mem_str[progparam_mem[pp]] != null) {
            System.out.printf(".%s\n",mem_str[progparam_mem[pp]]);
          } else {
            System.out.printf(".m%d\n",progparam_mem[pp]);
          }
          nc=true;
          break;
        case T_PSH | TM_PC:
          if (nc) System.out.printf(" ");
          //System.out.printf("\nPC\n");
          System.out.printf("\n^\n");
          nc=false;
          break;
        case T_STO | TM_PC:
          if (nc) System.out.printf(" ");
          //System.out.printf(".PC\n");
          System.out.printf(".^\n");
          nc=false;
          break;
        case T_POP:
          //System.out.printf("!");
          System.out.printf("..");
          nc=false;
          break;
        case T_END:
          System.out.printf("\n##\n");
          nc=false;
          return 0;
      }

      pp++;
    } /* mainloop */
  }

  public static void main(String args[]) {
    new mpl0run(args[0]);
  }

}

