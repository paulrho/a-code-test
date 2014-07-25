
class mpars {

  public mpars() {
    System.out.printf("mpars\n");
    test();
  }

  String assem = 
    "0 .m0\n"+
    ":L2 m3,m0,sin+.m3,0 .m1\n"+
    ":L9 m4'm1+@m3,m1,cos*+m4'm1+.&\n"+
    "m1004'm1+@m3,m1,sin*+m1004'm1+.&\n"+
    "m1,999<?L44,m1,1+.m1#L9\n"+
    ":L44 m0,999<?L53,m0,1+.m0#L2\n"+
    ":L53 ##\n";

  static final int T_PSH=0, T_FNC=1, T_PRF=2, T_JMP=3, T_STO=4, T_END=5, T_POP=6, T_BEQ=7;
  //static final int TM_MEM=256, TM_MEMIND=512, TM_INT=1024, TM_IMM_I=2048, TM_IMM_D=4096;
  static final int TM_MEM=1<<5, TM_MEMIND=2<<5, TM_INT=3<<5, TM_IMM_I=4<<5, TM_IMM_D=5<<5;
  static final int O_LT=0, O_PLUS=1, O_MULT=2;
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
    static final int M_NONE=0, M_VAR=1, M_NUM=2, M_STORE=3, M_LABEL=4, M_ADDR=5, M_OP=6, M_READADDR=7, M_JUMP=8, M_BRANCH=9, M_STOADDR=10;
    int f=M_NONE;
    int m=M_NONE;
    String build="";
    char op=' ';
  
  void printinstr() {
        if (f==M_OP) {
            System.out.printf("   %-8s : PRF %c\n","",op);
        } else if (f==M_STOADDR) {
            System.out.printf("   %-8s : STO []\n","");
        } else if (f==M_READADDR) {
            System.out.printf("   %-8s : FNC []\n","");
        } else if (m==M_NUM || m==M_VAR) {
          //System.out.printf("             got %s %d %d op=%c\n",build,m,f,op);
          if (f==M_LABEL) {
            System.out.printf("  :%-8s : LABEL\n",build);
          } else if (m==M_NUM && f==M_NONE) {
            System.out.printf("   %-8s : PSH# %s\n","",build);
          } else if (m==M_VAR && f==M_JUMP) {
              System.out.printf("   %-8s : JMP %s\n","",build);
          } else if (m==M_VAR && f==M_BRANCH) {
              System.out.printf("   %-8s : BEQ %s\n","",build);
          } else if (m==M_VAR && f==M_ADDR) {
              System.out.printf("   %-8s : PSH [%s]\n","",build);
          } else if (m==M_VAR && f==M_STORE) {
              System.out.printf("   %-8s : STO %s\n","",build);
          } else if (m==M_VAR && f==M_NONE) {
            // check for functions
            if (build.equals("sin") || build.equals("cos")) {
              System.out.printf("   %-8s : FNC %s\n","",build);
            } else {
              System.out.printf("   %-8s : PSH %s\n","",build);
            }
          }
        }
        m=M_NONE;
        f=M_NONE;
        op=' ';
        build="";
  }

  void assemble(String s) {
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
    // j=0
		// 27 seconds on apple mac (redo with latest changes)
		// 33 seconds on work machine seconds 

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

    prog[pp]=T_END; pp++;

		System.out.printf("Program steps %d\n",pp);

    System.out.printf("-------------------------\n");
    assemble(assem);
    System.out.printf("-------------------------\n");
    print_prog(0);
    System.out.printf("\n-------------------------\n");

    for (memi[mem_i]=0; memi[mem_i]<numobj; ++memi[mem_i]) {
      memi[mem_x+memi[mem_i]]=57+memi[mem_i];
      memi[mem_y+memi[mem_i]]=57-memi[mem_i];
    }
    for (memi[mem_iter]=0; memi[mem_iter]</*2*/ 100; ++memi[mem_iter]) {
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
      System.out.printf("part=%f\n",mem[mem_v]);
    }
    System.out.printf("%f\n",mem[mem_v]);

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
            System.out.printf("\n:L%d ",pp);
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
          System.out.printf("m%d'",progparam_mem[pp]);
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
        case T_PSH:
          if (nc) System.out.printf(",");
          System.out.printf("m%d",progparam_mem[pp]);
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
            case O_MULT:
              System.out.printf("*");
              break;
          }
          nc=false;
          break;
        case T_BEQ:
          //System.out.printf("?(%d)",progparam_mem[pp]);
          System.out.printf("?L%d",progparam_mem[pp]);
          nc=true;
          break;
        case T_JMP:
          //System.out.printf("#(%d)",progparam_mem[pp]);
          System.out.printf("#L%d",progparam_mem[pp]);
          nc=true;
          break;
        case T_STO | TM_MEMIND:
          System.out.printf(".&\n");
          nc=false;
          break;
        case T_STO:
          if (nc) System.out.printf(" ");
          System.out.printf(".m%d",progparam_mem[pp]);
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

