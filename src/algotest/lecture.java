package algotest;

public class lecture {
	//private int lecnum;
	public String lecname;
	/*private char lectype;

	private int lecpeople;*/
	public String lectime;
	public int lec_credit;
	/*private char lecprof;
	private char lecroom;
	private char leclevel;*/
	
	public lecture(){
		this("","",0);
	}

	public lecture(String lecname, String lectime,int lec_credit) {
		// TODO Auto-generated constructor stub
		if (lecname==null)
			throw new IllegalArgumentException("lecture name must be input");
		if (lectime==null)
			throw new IllegalArgumentException("lecture time must be input");
		if (lectime==null)
			throw new IllegalArgumentException("lecture credit must be input");

		this.lecname=lecname;
		this.lectime=lectime;
		this.lec_credit=lec_credit;

	}


	
}
