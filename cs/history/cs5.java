class cs {

cs() {

  long COUNT=14000*2000;
  double l;
  //for (long i=1; i<COUNT; ++i) l=Math.sin(2450000.0);
  for (long i=1; i<COUNT; ++i) l=Math.sin(2.0);
  System.out.printf("Total %d\n",COUNT);
}
  
  public static void main(String args[]) {
    new cs();
  }


}
