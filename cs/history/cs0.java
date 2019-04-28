class cs {


double dt;
double AU;

void moonpos(double JD) {
double l=5.162e-3*Math.sin(JD)+5e-3*Math.sin(JD+JD);
l=l+3.862e-3*Math.sin(4.0*JD)+9.049e-3*Math.sin(2.6*JD);
l=l+4.862e-3*Math.sin(4.1*JD)+8.049e-3*Math.sin(2.5*JD);
l=l+5.862e-3*Math.sin(4.2*JD)+7.049e-3*Math.sin(2.4*JD);
l=l+6.862e-3*Math.sin(4.3*JD)+6.049e-3*Math.sin(2.3*JD);
l=l+7.862e-3*Math.sin(4.4*JD)+5.049e-3*Math.sin(2.2*JD);
l=l+8.862e-3*Math.sin(4.5*JD)+4.049e-3*Math.sin(2.1*JD);
}

cs() {

  dt=180.0/Math.PI;
  AU=1.0;
  long COUNT=14000*2;
  //430 COUNT=14000*2
  //440 fori=1toCOUNT:JD=2450000.0:gosub1000:next
  for (int j=1; j<10*10; ++j) {
      for (long i=1; i<COUNT; ++i) moonpos(2450000.0);
  }
  System.out.printf("Total %d\n",COUNT*10*10);
}
  
  public static void main(String args[]) {
    new cs();
  }


}
