class cs {


void moonpos(double JD) {
double l=0.0;
l=l+Math.sin(JD);
l=l+Math.sin(JD);
l=l+Math.sin(JD);
l=l+Math.sin(JD);
l=l+Math.sin(JD);
l=l+Math.sin(JD);
l=l+Math.sin(JD);
l=l+Math.sin(JD);
l=l+Math.sin(JD);
l=l+Math.sin(JD);
l=l+Math.sin(JD);
l=l+Math.sin(JD);
l=l+Math.sin(JD);
l=l+Math.sin(JD);
l=l+Math.sin(JD);
l=l+Math.sin(JD);
l=l+Math.sin(JD);
l=l+Math.sin(JD);
l=l+Math.sin(JD);
l=l+Math.sin(JD);
}

cs() {

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
