package algotest;

public class lecture {
    public String lecname;
    public String lectime;
    public int lec_credit;
    public String lec_cd;

    public lecture() {
        this("", "", 0,"");
    }

    public lecture(String lecname, String lectime, int lec_credit,String lec_cd) {
        // TODO Auto-generated constructor stub
        if (lecname == null){
            throw new IllegalArgumentException("lecture name must be input");}
        if (lectime == null){
            throw new IllegalArgumentException("lecture time must be input");}
        if (lec_credit == 0){
            throw new IllegalArgumentException("lecture credit must be input");}

        this.lecname = lecname;
        this.lectime = lectime;
        this.lec_credit = lec_credit;
        this.lec_cd=lec_cd;

    }



}
