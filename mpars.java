
class mpars {

  public mpars() {
    System.out.printf("mpars\n");
    test();
  }

    enum progtype { T_PSH, T_FNC, T_PRF };

  void test() {
    int MAXPROG=1000;
    int MAXMEM=3000;
    int MAXSTK=100;
    int numobj=1000;
    int prog[]=new int[MAXPROG];  int pp=0;
    double progparam_d[]=new double[MAXPROG];
    int progparam_mem[]=new int[MAXPROG]; 
    double mem[]=new double[MAXMEM];    int mp=0;
    int memi[]=new int[MAXMEM];   
    double stk[]=new double[MAXSTK]; int sp=0;
    

    int mem_i=mp++;
    int mem_j=mp++;
    int mem_iter=mp++;
    int mem_v=mp++;
    int mem_x=mp; mp+=numobj;
    int mem_y=mp; mp+=numobj;

    prog[pp]=T_PSH; progparam_mem[pp]=-1; progparam_i=0; pp++;

    prog[pp]=T_PSH; progparam_mem[pp]=mem_i; pp++;
    prog[pp]=T_PSH; progparam_mem[pp]=-1; progparam_i=numobj; pp++;
    prog[pp]=T_PRF; progparam_mem[pp]=100; pp++;

    for (memi[mem_i]=0; memi[mem_i]<numobj; ++memi[mem_i]) {
      memi[mem_x+memi[mem_i]]=57+memi[mem_i];
      memi[mem_y+memi[mem_i]]=57-memi[mem_i];
    }
    for (memi[mem_iter]=0; memi[mem_iter]<100; ++memi[mem_iter]) {
      for (memi[mem_i]=0; memi[mem_i]<1000; ++memi[mem_i]) {
        mem[mem_v]=mem[mem_v]+Math.sin(memi[mem_i]);
        for (memi[mem_j]=0; memi[mem_j]<numobj; ++memi[mem_j]) {
          mem[mem_x+memi[mem_j]]=mem[mem_x+memi[mem_j]]+mem[mem_v]*Math.cos(memi[mem_j]*1.0);
          mem[mem_y+memi[mem_j]]=mem[mem_y+memi[mem_j]]+mem[mem_v]*Math.sin(memi[mem_j]*1.0);
        }
      }
      System.out.printf("part=%f\n",mem[mem_v]);
    }
    System.out.printf("%f\n",mem[mem_v]);


    
  }

  public static void main(String args[]) {
    new mpars();
  }

}

