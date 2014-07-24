
class mpars {

  public mpars() {
    System.out.printf("mpars\n");
    test();
  }

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

  public static void main(String args[]) {
    new mpars();
  }

}

