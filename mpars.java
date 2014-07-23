
class mpars {

  public mpars() {
    System.out.printf("mpars\n");
    test();
  }

  static final int T_PSH=0, T_FNC=1, T_PRF=2, T_JMP=3, T_STO=4, T_END=5, T_POP=6;
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
    // j=0
    prog[pp]=T_PSH; progparam_mem[pp]=-1; progparam_i[pp]=0; pp++;
    prog[pp]=T_STO; progparam_mem[pp]=mem_j; pp++;
      // j loop
    prog[pp]=T_PSH; progparam_mem[pp]=mem_x+memi[mem_j]; pp++;
    prog[pp]=T_PSH; progparam_mem[pp]=mem_v; pp++;
    prog[pp]=T_PSH; progparam_mem[pp]=memi[mem_j]; pp++;
    prog[pp]=T_FNC; progparam_mem[pp]=F_cos; pp++;
    prog[pp]=T_PRF; progparam_mem[pp]=O_MULT; pp++;
    prog[pp]=T_PRF; progparam_mem[pp]=O_PLUS; pp++;
    prog[pp]=T_STO; progparam_mem[pp]=mem_x+memi[mem_j]; pp++;

    prog[pp]=T_PSH; progparam_mem[pp]=mem_y+memi[mem_j]; pp++;
    prog[pp]=T_PSH; progparam_mem[pp]=mem_v; pp++;
    prog[pp]=T_PSH; progparam_mem[pp]=memi[mem_j]; pp++;
    prog[pp]=T_FNC; progparam_mem[pp]=F_sin; pp++;
    prog[pp]=T_PRF; progparam_mem[pp]=O_MULT; pp++;
    prog[pp]=T_PRF; progparam_mem[pp]=O_PLUS; pp++;
    prog[pp]=T_STO; progparam_mem[pp]=mem_y+memi[mem_j]; pp++;
       
    // j<numobj
    prog[pp]=T_PSH; progparam_mem[pp]=mem_j; pp++;
    prog[pp]=T_PSH; progparam_mem[pp]=-1; progparam_i[pp]=numobj; pp++;
    prog[pp]=T_PRF; progparam_mem[pp]=O_LT; pp++;
    prog[pp]=T_POP; pp++;
    // jmp out
    prog[pp]=T_JMP; progparam_mem[pp]=pp+6; pp++;
    // j++
    prog[pp]=T_PSH; progparam_mem[pp]=mem_j; pp++;
    prog[pp]=T_PSH; progparam_mem[pp]=-1; progparam_i[pp]=1; pp++;
    prog[pp]=T_PRF; progparam_mem[pp]=O_PLUS; pp++;
    prog[pp]=T_STO; progparam_mem[pp]=mem_j; pp++;
    // jmp to top j
    prog[pp]=T_JMP; progparam_mem[pp]=1; pp++;
    // out:
    prog[pp]=T_END; progparam_mem[pp]=1; pp++;

    for (memi[mem_i]=0; memi[mem_i]<numobj; ++memi[mem_i]) {
      memi[mem_x+memi[mem_i]]=57+memi[mem_i];
      memi[mem_y+memi[mem_i]]=57-memi[mem_i];
    }
    for (memi[mem_iter]=0; memi[mem_iter]<100; ++memi[mem_iter]) {
      for (memi[mem_i]=0; memi[mem_i]<1000; ++memi[mem_i]) {
        mem[mem_v]=mem[mem_v]+Math.sin(memi[mem_i]);
        //for (memi[mem_j]=0; memi[mem_j]<numobj; ++memi[mem_j]) {
          //mem[mem_x+memi[mem_j]]=mem[mem_x+memi[mem_j]]+mem[mem_v]*Math.cos(memi[mem_j]*1.0);
          //mem[mem_y+memi[mem_j]]=mem[mem_y+memi[mem_j]]+mem[mem_v]*Math.sin(memi[mem_j]*1.0);
        //}
        // rn sub program from start
        run_prog(0);

      }
      System.out.printf("part=%f\n",mem[mem_v]);
    }
    System.out.printf("%f\n",mem[mem_v]);


    
  }

  int verbose=0;

  int run_prog(int pp) {
    int instr;

    mainloop:
    while(true) {
      instr=prog[pp];
  //public enum ProgType { T_PSH, T_FNC, T_PRF, T_JMP, T_STO, T_END };
      if (verbose>0) { System.out.printf("%06d Got instr =%d\n",pp,instr); }
      switch (instr) {
        case T_PSH:
             //prog[pp]=T_PSH; progparam_mem[pp]=mem_j; pp++;
             //prog[pp]=T_PSH; progparam_mem[pp]=-1; progparam_i[pp]=numobj; pp++;
          if (progparam_mem[pp]==-1) {
            /* immediate int */
            stk[sp]=progparam_i[pp];
          } else if (progparam_mem[pp]==-1) {
            /* immediate d*/
            stk[sp]=progparam_d[pp];
          } else {
            stk[sp]=mem[progparam_mem[pp]];
          }
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
              stk[sp-2]=(stk[sp-2]<stk[sp-1])?-1:0;
              break;
            case O_PLUS:
              stk[sp-2]=stk[sp-2]+stk[sp-1];
              break;
            case O_MULT:
              stk[sp-2]=stk[sp-2]*stk[sp-1];
              break;
          }
          sp--;
          if (verbose>0) System.out.printf("  sp=%d\n",sp);
          break;
        case T_JMP:
          pp=progparam_mem[pp];
          continue mainloop;
        case T_STO:
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

