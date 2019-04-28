class cs {

cs() {

  //long COUNT=14000*2;
  long COUNT=100;
  double l;
  //for (long i=1; i<COUNT; ++i) l=Math.sin(2450000.0);
  for (long i=1; i<COUNT; ++i)
    for (long j=0; j<1000000; ++j) l=Math.sin(2.0+j/1000000.0);
  System.out.printf("Total %dM\n",COUNT);
}
  
  public static void main(String args[]) {
    new cs();
  }


}
