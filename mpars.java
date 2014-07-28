
class mpars {

  public mpars() {
    System.out.printf("mpars\n");
    test();
  }

/*
    for (memi[mem_i]=0; memi[mem_i]<numobj; ++memi[mem_i]) {
      memi[mem_x+memi[mem_i]]=57+memi[mem_i];
      memi[mem_y+memi[mem_i]]=57-memi[mem_i];
    }
*/
  String assem = 
    ":i=1\n"+        // i
    ":m1=1\n"+        // j
    ":m2=1\n"+        // iter
    ":m3=1\n"+        // v
    ":m4=1000\n"+     // x[1000]
    ":m1004=1000\n"+  // y[1000]
    "0 .m0 :Li\n"+
    "57,m0+m4'm0+.&\n"+
    "57,m0-m1004'm0+.&\n"+
    "m0,999<?Li_,m0,1+.m0#Li:Li_\n"+
    ////----------
    "0 .m2\n"+
    ":L2 0 .m0\n"+
    ":LI m3,m0,sin+.m3,0 .m1\n"+
    ":L11 m4'm1+@m3,m1,cos*+m4'm1+.&\n"+
    "m1004'm1+@m3,m1,sin*+m1004'm1+.&\n"+
    "m1,999<?L46,m1,1+.m1#L11\n"+
    ":L46 m0,999<?LI_,m0,1+.m0#LI\n"+
    ":LI_ m3~m2,99<?L65,m2,1+.m2#L2\n"+
    ":L65 m3~##\n";

  static final int T_PSH=0, T_FNC=1, T_PRF=2, T_JMP=3, T_STO=4, T_END=5, T_POP=6, T_BEQ=7, T_PRINT=8;
  static final int TM_MEM=1<<5, TM_MEMIND=2<<5, TM_INT=3<<5, TM_IMM_I=4<<5, TM_IMM_D=5<<5;
  static final int O_LT=0, O_PLUS=1, O_MULT=2, O_MINUS=3;
  static final int F_cos=0, F_sin=1;

    int MAXPROG=1000;
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
    
/*
   -------------------------
   0 .m0
   :L2 m3,m0,sin+.m3,0 .m1
   :L9 m4'm1+@m3,m1,cos*+m4'm1+.&
   m1004'm1+@m3,m1,sin*+m1004'm1+.&
   m1,999<?L44,m1,1+.m1#L9
   :L44 m0,999<?L53,m0,1+.m0#L2
   :L53 ##
   -------------------------
 */
    static final int M_NONE=0, M_VAR=1, M_NUM=2, M_STORE=3, M_LABEL=4, M_ADDR=5, M_OP=6, M_READADDR=7, M_JUMP=8, M_BRANCH=9, M_STOADDR=10, M_PRINT=11, M_END=12, M_MEMORY=13;
    int f=M_NONE;
    int m=M_NONE;
    String build="";
    char op=' ';
  boolean writeprog=true;
  
  int strip_m(String s, int start) {
    int x;
    x=Integer.parseInt(s.substring(start));
    //System.out.printf("x=%d\n",x);
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
  void printinstr() {
        if (f==M_MEMORY && m==M_NUM) {
          System.out.printf("Got a memory %s and %s (%d)\n",keep_label,build,strip_m(build,0));
          if (writeprog) { mem_str[mp]=keep_label; mp+=strip_m(build,0); }
        } else if (f==M_OP) {
            System.out.printf("   %-8s : PRF %c\n","",op);
            if (writeprog && op=='<') { prog[pp]=T_PRF; progparam_mem[pp]=O_LT; pp++; }
            if (writeprog && op=='+') { prog[pp]=T_PRF; progparam_mem[pp]=O_PLUS; pp++; }
            if (writeprog && op=='-') { prog[pp]=T_PRF; progparam_mem[pp]=O_MINUS; pp++; }
            if (writeprog && op=='*') { prog[pp]=T_PRF; progparam_mem[pp]=O_MULT; pp++; }
        } else if (f==M_END) {
            System.out.printf("   %-8s : END\n","");
            if (writeprog) { prog[pp]=T_END; pp++; }
        } else if (f==M_PRINT) {
            System.out.printf("   %-8s : PRINT %s\n","",build);
            if (writeprog) { prog[pp]=T_PRINT; progparam_mem[pp]=strip_m(build,1); pp++; }
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
          } else if (m==M_NUM && f==M_NONE) {
            System.out.printf("   %-8s : PSH# %s\n","",build);
            if (writeprog) { prog[pp]=T_PSH|TM_IMM_I; progparam_i[pp]=strip_m(build,0); pp++; }
          } else if (m==M_VAR && f==M_JUMP) {
            System.out.printf("   %-8s : JMP %s\n","",build);
            //if (writeprog) { prog[pp]=T_JMP; progparam_mem[pp]=strip_m(build,1); pp++; }
            if (writeprog) { prog[pp]=T_JMP; progparam_mem[pp]=-1; progparam_str[pp]=new String(build); pp++; }
          } else if (m==M_VAR && f==M_BRANCH) {
            System.out.printf("   %-8s : BEQ %s\n","",build);
            //if (writeprog) { prog[pp]=T_BEQ; progparam_mem[pp]=strip_m(build,1); pp++; }
            if (writeprog) { prog[pp]=T_BEQ; progparam_mem[pp]=-1; progparam_str[pp]=new String(build); pp++; }
          } else if (m==M_VAR && f==M_ADDR) {
            System.out.printf("   %-8s : PSH [%s]\n","",build);
            if (writeprog) { prog[pp]=T_PSH | TM_MEM ; progparam_mem[pp]=strip_m(build,1); pp++; }
          } else if (m==M_VAR && f==M_STORE) {
            System.out.printf("   %-8s : STO %s\n","",build);
            if (writeprog) { prog[pp]=T_STO; progparam_mem[pp]=strip_m(build,1); pp++; }
          } else if (m==M_VAR && f==M_NONE) {
            // check for functions
            if (build.equals("sin")) {
              System.out.printf("   %-8s : FNC %s\n","",build);
              if (writeprog) { prog[pp]=T_FNC; progparam_mem[pp]=F_sin; pp++; }
            } else if (build.equals("cos")) {
              System.out.printf("   %-8s : FNC %s\n","",build);
              if (writeprog) { prog[pp]=T_FNC; progparam_mem[pp]=F_cos; pp++; }
            } else {
              System.out.printf("   %-8s : PSH %s\n","",build);
              if (writeprog) { prog[pp]=T_PSH ; progparam_mem[pp]=strip_m(build,1); pp++; }
            }
          }
        }
        m=M_NONE;
        f=M_NONE;
        op=' ';
        build="";
  }

  String keep_label="";

  void assemble(String s) {
        if (writeprog) { 
           pp=0;
           mp=0;
        }
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
      if (c=='@') {
        printinstr();
        f=M_READADDR;
        printinstr();
        continue;
      }
      if (c=='.') {
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
      if (m==M_NUM || m==M_VAR && (c>='a' && c<='z' || c>='A' && c<='Z' || c>='0' && c<='9' || c=='_')) {
        build+=c+"";
        continue;
      }
    }
    if (writeprog) {
      int i;
      int p;
      // perform label substitions
      for (p=0; p<pp; ++p) {
        if (prog[p]==T_JMP || prog[p]==T_BEQ) {
          for (i=0; i<labeltop; ++i) {
            if (labelstr[i].equals(progparam_str[p])) {
              System.out.printf("Found label %s at location %d\n",labelstr[i],labelint[i]);
              progparam_mem[p]=labelint[i];
            }
          }
        }
      }
    }
  }

  void test() {
    int mem_i=mp++;
    int mem_j=mp++;
    int mem_iter=mp++;
    int mem_v=mp++;
    int mem_x=mp; mp+=numobj;
    int mem_y=mp; mp+=numobj;
		int pploop1;
		int pploop2;
		int pploop3;
    // j=0
		// 27 seconds on apple mac (redo with latest changes)
		// 33 seconds on work machine seconds 

        //for (memi[mem_iter]=0; memi[mem_iter]</*2*/ 100; ++memi[mem_iter]) {
        prog[pp]=T_PSH|TM_IMM_I; progparam_i[pp]=0; pp++;
        prog[pp]=T_STO; progparam_mem[pp]=mem_iter; pp++;
          // iter loop
          pploop3=pp;
      
          //for (memi[mem_i]=0; memi[mem_i]</*2*/ 1000 ; ++memi[mem_i]) {
          prog[pp]=T_PSH|TM_IMM_I; progparam_i[pp]=0; pp++;
          prog[pp]=T_STO; progparam_mem[pp]=mem_i; pp++;
            // i loop
		        pploop2=pp;

            //mem[mem_v]=mem[mem_v]+Math.sin(memi[mem_i]);
            prog[pp]=T_PSH ; progparam_mem[pp]=mem_v; pp++;
            //prog[pp]=T_PSH |TM_INT; progparam_mem[pp]=mem_i; pp++;
            prog[pp]=T_PSH ; progparam_mem[pp]=mem_i; pp++;
            prog[pp]=T_FNC; progparam_mem[pp]=F_sin; pp++;
            prog[pp]=T_PRF; progparam_mem[pp]=O_PLUS; pp++;
            prog[pp]=T_STO; progparam_mem[pp]=mem_v; pp++;
        
            prog[pp]=T_PSH|TM_IMM_I; progparam_i[pp]=0; pp++;
            prog[pp]=T_STO; progparam_mem[pp]=mem_j; pp++;
              // j loop
		          pploop1=pp;
              prog[pp]=T_PSH | TM_MEM ; progparam_mem[pp]=mem_x; pp++;
              prog[pp]=T_PSH ; progparam_mem[pp]=mem_j; pp++;
              prog[pp]=T_PRF; progparam_mem[pp]=O_PLUS; pp++;
              prog[pp]=T_FNC | TM_MEMIND ; pp++;   // read stack, convert this to memory address and read that // note about doubles
              prog[pp]=T_PSH; progparam_mem[pp]=mem_v; pp++;
              prog[pp]=T_PSH; progparam_mem[pp]=mem_j; pp++;
              prog[pp]=T_FNC; progparam_mem[pp]=F_cos; pp++;
              prog[pp]=T_PRF; progparam_mem[pp]=O_MULT; pp++;
              prog[pp]=T_PRF; progparam_mem[pp]=O_PLUS; pp++;
          
              prog[pp]=T_PSH | TM_MEM ; progparam_mem[pp]=mem_x; pp++;
              prog[pp]=T_PSH ; progparam_mem[pp]=mem_j; pp++;
              prog[pp]=T_PRF; progparam_mem[pp]=O_PLUS; pp++;
              prog[pp]=T_STO | TM_MEMIND ; pp++;   // read stack, convert this to memory address and write the earlier on stack (-2) that // note about doubles
              //prog[pp]=T_STO; progparam_mem[pp]=mem_x+memi[mem_j]; pp++;
          
		          //
              prog[pp]=T_PSH | TM_MEM ; progparam_mem[pp]=mem_y; pp++;
              prog[pp]=T_PSH ; progparam_mem[pp]=mem_j; pp++;
              prog[pp]=T_PRF; progparam_mem[pp]=O_PLUS; pp++;
              prog[pp]=T_FNC | TM_MEMIND ; pp++;   // read stack, convert this to memory address and read that // note about doubles
              prog[pp]=T_PSH; progparam_mem[pp]=mem_v; pp++;
              prog[pp]=T_PSH; progparam_mem[pp]=mem_j; pp++;
              prog[pp]=T_FNC; progparam_mem[pp]=F_sin; pp++;
              prog[pp]=T_PRF; progparam_mem[pp]=O_MULT; pp++;
              prog[pp]=T_PRF; progparam_mem[pp]=O_PLUS; pp++;
          
              prog[pp]=T_PSH | TM_MEM ; progparam_mem[pp]=mem_y; pp++;
              prog[pp]=T_PSH ; progparam_mem[pp]=mem_j; pp++;
              prog[pp]=T_PRF; progparam_mem[pp]=O_PLUS; pp++;
              prog[pp]=T_STO | TM_MEMIND ; pp++;   // read stack, convert this to memory address and write the earlier on stack (-2) that // note about doubles
               
            // j<numobj
            prog[pp]=T_PSH; progparam_mem[pp]=mem_j; pp++;
            prog[pp]=T_PSH|TM_IMM_I; progparam_i[pp]=numobj-1; pp++;
            prog[pp]=T_PRF; progparam_mem[pp]=O_LT; pp++;
            // jmp out
            prog[pp]=T_BEQ; progparam_mem[pp]=pp+6; pp++;
            // j++
            prog[pp]=T_PSH; progparam_mem[pp]=mem_j; pp++;
            prog[pp]=T_PSH|TM_IMM_I; progparam_i[pp]=1; pp++;
            prog[pp]=T_PRF; progparam_mem[pp]=O_PLUS; pp++;
            prog[pp]=T_STO; progparam_mem[pp]=mem_j; pp++;
            // jmp to top j
            prog[pp]=T_JMP; progparam_mem[pp]=pploop1; pp++;
            // out:
          // i<numobj
          prog[pp]=T_PSH; progparam_mem[pp]=mem_i; pp++;
          prog[pp]=T_PSH|TM_IMM_I; progparam_i[pp]=numobj-1; pp++;
          prog[pp]=T_PRF; progparam_mem[pp]=O_LT; pp++;
          // jmp out
          prog[pp]=T_BEQ; progparam_mem[pp]=pp+6; pp++;
          // i++
          prog[pp]=T_PSH; progparam_mem[pp]=mem_i; pp++;
          prog[pp]=T_PSH|TM_IMM_I; progparam_i[pp]=1; pp++;
          prog[pp]=T_PRF; progparam_mem[pp]=O_PLUS; pp++;
          prog[pp]=T_STO; progparam_mem[pp]=mem_i; pp++;
          // jmp to top i
          prog[pp]=T_JMP; progparam_mem[pp]=pploop2; pp++;
          // out:
        prog[pp]=T_PRINT; progparam_mem[pp]=mem_v; pp++;
          
        // iter<100
        prog[pp]=T_PSH; progparam_mem[pp]=mem_iter; pp++;
        prog[pp]=T_PSH|TM_IMM_I; progparam_i[pp]=100-1; pp++;
        prog[pp]=T_PRF; progparam_mem[pp]=O_LT; pp++;
        // jmp out
        prog[pp]=T_BEQ; progparam_mem[pp]=pp+6; pp++;
        // iter++
        prog[pp]=T_PSH; progparam_mem[pp]=mem_iter; pp++;
        prog[pp]=T_PSH|TM_IMM_I; progparam_i[pp]=1; pp++;
        prog[pp]=T_PRF; progparam_mem[pp]=O_PLUS; pp++;
        prog[pp]=T_STO; progparam_mem[pp]=mem_iter; pp++;
        // jmp to top iter
        prog[pp]=T_JMP; progparam_mem[pp]=pploop3; pp++;
        // out:
      prog[pp]=T_PRINT; progparam_mem[pp]=mem_v; pp++;

    prog[pp]=T_END; pp++;

		System.out.printf("Program steps %d\n",pp);

    System.out.printf("--print-prog----------------------\n");
    print_prog(0);
    System.out.printf("\n-------------------------\n");
    writeprog=true; assemble(assem);
    System.out.printf("--print-prog----------------------\n");
    print_prog(0);
    System.out.printf("\n-------------------------\n");
    writeprog=false;
    assemble(assem);
    System.out.printf("\n-------------------------\n");

    //for (memi[mem_i]=0; memi[mem_i]<numobj; ++memi[mem_i]) {
      //memi[mem_x+memi[mem_i]]=57+memi[mem_i];
      //memi[mem_y+memi[mem_i]]=57-memi[mem_i];
    //}
    //for (memi[mem_iter]=0; memi[mem_iter]</*2*/ 100; ++memi[mem_iter]) {
      //for (memi[mem_i]=0; memi[mem_i]</*2*/ 1000 ; ++memi[mem_i]) {
        //mem[mem_v]=mem[mem_v]+Math.sin(memi[mem_i]);
        //for (memi[mem_j]=0; memi[mem_j]<numobj; ++memi[mem_j]) {
          //mem[mem_x+memi[mem_j]]=mem[mem_x+memi[mem_j]]+mem[mem_v]*Math.cos(memi[mem_j]*1.0);
          //mem[mem_y+memi[mem_j]]=mem[mem_y+memi[mem_j]]+mem[mem_v]*Math.sin(memi[mem_j]*1.0);
        //}
        // rn sub program from start
        run_prog(0);
         //System.out.printf("Total instructions = %d\n",ic);

      //}
      //System.out.printf("part=%f\n",mem[mem_v]);
    //}
    //System.out.printf("%f\n",mem[mem_v]);

   // display values for comparison
   for (memi[mem_j]=0; memi[mem_j]<numobj; ++memi[mem_j]) {
      System.out.printf("  %d]%f\n",memi[mem_j],mem[mem_x+memi[mem_j]]);
   }
    
  }

  int verbose=0;
  int ic=0;

  int run_prog(int pp) {
    int instr;

    mainloop:
    while(true) {
      instr=prog[pp];
      ic++;
  //public enum ProgType { T_PSH, T_FNC, T_PRF, T_JMP, T_STO, T_END };
      if (verbose>0) { System.out.printf("%06d Got instr =%d\n",pp,instr); }
      switch (instr) {
        case T_PSH|TM_MEM:
            stk[sp]=progparam_mem[pp];
						sp++;
				  break;
        case T_FNC|TM_MEMIND:
            stk[sp-1]=mem[(int)stk[sp-1]];
				  break;
        case T_PSH|TM_INT:
          stk[sp]=memi[progparam_mem[pp]];
				  sp++;
				  break;
        case T_PSH|TM_IMM_I:
          /* immediate int */
          stk[sp]=progparam_i[pp];
          sp++;
          break;
        case T_PSH|TM_IMM_D:
          /* immediate d*/
          stk[sp]=progparam_d[pp];
          sp++;
          break;
        case T_PRINT:
          System.out.printf("%f\n",mem[progparam_mem[pp]]);
          break;
        case T_PSH:
          stk[sp]=mem[progparam_mem[pp]];
          sp++;
          break;
        case T_FNC:
          switch (progparam_mem[pp]) {
            case F_sin:
              stk[sp-1]=Math.sin(stk[sp-1]);
              break;
            case F_cos:
              stk[sp-1]=Math.cos(stk[sp-1]);
              break;
          }
          break;
        case T_PRF:
             //static final int O_LT=0, O_PLUS=1, O_MULT=2;
          if (verbose>0) { System.out.printf("  Performing op %d\n",progparam_mem[pp]); }
          switch (progparam_mem[pp]) {
            case O_LT:
              if (verbose>0) { System.out.printf("%f < %f\n",stk[sp-2],stk[sp-1]); }
              stk[sp-2]=(stk[sp-2]<stk[sp-1])?-1:0;
              if (verbose>0) { System.out.printf(" = %f\n",stk[sp-2]); }
              break;
            case O_PLUS:
              if (verbose>0) { System.out.printf(" %f + %f\n",stk[sp-2],stk[sp-1]); }
              stk[sp-2]=stk[sp-2]+stk[sp-1];
              if (verbose>0) { System.out.printf(" = %f\n",stk[sp-2]); }
              break;
            case O_MINUS:
              if (verbose>0) { System.out.printf(" %f - %f\n",stk[sp-2],stk[sp-1]); }
              stk[sp-2]=stk[sp-2]-stk[sp-1];
              if (verbose>0) { System.out.printf(" = %f\n",stk[sp-2]); }
              break;
            case O_MULT:
              if (verbose>0) { System.out.printf(" %f * %f\n",stk[sp-2],stk[sp-1]); }
              stk[sp-2]=stk[sp-2]*stk[sp-1];
              if (verbose>0) { System.out.printf(" = %f\n",stk[sp-2]); }
              break;
          }
          sp--;
          if (verbose>0) System.out.printf("  sp=%d\n",sp);
          break;
        case T_BEQ:
          sp--;
          if (stk[sp]==0) {
            pp=progparam_mem[pp];
            continue mainloop;
          }
          break;
        case T_JMP:
          pp=progparam_mem[pp];
          continue mainloop;
        case T_STO | TM_MEMIND:
          if (verbose>0) { System.out.printf("Storing IND %f\n",stk[sp-1]); }
          mem[(int)stk[sp-1]]=stk[sp-2];
          sp--;
          sp--;
          break;
        case T_STO:
          if (verbose>0) { System.out.printf("Storing %f\n",stk[sp-1]); }
          mem[progparam_mem[pp]]=stk[sp-1];
          sp--;
          break;
        case T_POP:
          sp--;
          break;
        case T_END:
          return 0;
      }

      pp++;
    } /* mainloop */
  }

  int print_prog(int pp) {
    int instr;
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
      instr=prog[pp];
      ic++;
      switch (instr) {
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
            System.out.printf("%s~",mem_str[progparam_mem[pp]]);
          } else {
            System.out.printf("%s~",progparam_mem[pp]);
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
            System.out.printf(".%s",mem_str[progparam_mem[pp]]);
          } else {
            System.out.printf(".m%d",progparam_mem[pp]);
          }
          nc=true;
          break;
        case T_POP:
          System.out.printf("!");
          nc=false;
          break;
        case T_END:
          System.out.printf("##");
          nc=false;
          return 0;
      }

      pp++;
    } /* mainloop */
  }

  public static void main(String args[]) {
    new mpars();
  }

}

