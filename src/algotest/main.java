package algotest;
import java.util.*;

class lecturealgo{
    static int total_leccredit=0;

    //declare fixed timetable array
    static ArrayList<lecture> f_lecs=new ArrayList<lecture>();
    static ArrayList<ArrayList<lecture>> combinations=new ArrayList<ArrayList<lecture>>();

    public static void main(String[] args){
        //declare arraylist
        ArrayList<lecture> lecs = new ArrayList<lecture>();
        HashMap<Integer,Double> cohesion_checked_list = new HashMap<Integer, Double>();
        ValueComparator bvc = new ValueComparator(cohesion_checked_list);
        TreeMap<Integer, Double> sorted_map = new TreeMap<Integer, Double>(bvc);


        Scanner n=new Scanner(System.in);

        System.out.print("How many credits do you want? : ");
        int credits=n.nextInt();

        //add classes to arraylist value "lecs"
        lecs.add( new lecture("linux","월A,화B",3) );
        lecs.add( new lecture("linux","목A,금B",3) );
        lecs.add( new lecture("data structure","월C,월D",3) );
        lecs.add( new lecture("java","월E,화E",3) );
        lecs.add( new lecture("java","수E,목E",3) );
        lecs.add( new lecture("database","수1,수2,화3,화4",3) );
        lecs.add( new lecture("algorithm","수A,목B",3) );
        lecs.add( new lecture("algorithm","금A,금B",3) );
        lecs.add( new lecture("software analyze&design","월6,월7,금6,금7",3) );
        lecs.add( new lecture("mobile programming","목B,화B",3) );
        lecs.add( new lecture("python","수A,수B",3) );
        lecs.add( new lecture("python","화A,화B",3) );
        lecs.add( new lecture("web programming","목1,목2,목3,목4",3) );
        lecs.add( new lecture("server programming","목A,수B",3) );
        lecs.add( new lecture("embedded","화C,화D",3) );
        lecs.add( new lecture("discrete mathmatics","목1,목2,목3,목4",3) );
        lecs.add( new lecture("program logic","목C,목D",3) );
        lecs.add( new lecture("computer engineering for begineer","화3,화4,화5",3) );
        lecs.add( new lecture("operating system","월7,월8,화7,화8",3) );
        lecs.add( new lecture("edu-eco seminar","금B,목B",3) );
        lecs.add( new lecture("hacking test","목1,목2,화3,화4",3) );
        lecs.add( new lecture("Total Project","월A,화B",3) );
        lecs.add( new lecture("IT and Foundation","월A,화B",3));
        lecs.add( new lecture("General theory of commercial law","수C,목D",3) );
        lecs.add( new lecture("Itemize discussion of receivables","월B,화A",3) );
        lecs.add( new lecture("the Civil Procedure Code (CPC)","수A,화B",3) );
        lecs.add( new lecture("intellectual property law","금C,금B",3) );
        lecs.add( new lecture("philosophy of law","목A,목B",3) );


        //System.out.println(lecs.size());
        for (int num=0;num<50;num++) {//how
            double cohesion=0.0;
            total_leccredit=0;
            Collections.shuffle(lecs);
            f_lecs.clear();
            f_lecs.add(lecs.get(0));
            total_leccredit+=f_lecs.get(0).lec_credit;


            for (int i=1;i<lecs.size();i++){

                if(credits==total_leccredit){
                    if (!duplication_check(f_lecs)){
                        System.out.println(num+" is duplicated to\n");
                        break;
                    }
                    else{
                        //deep copy. shallow copy는 clear시 연결되어 같이 초기화됨.
                        combinations.add((ArrayList<lecture>)f_lecs.clone());
                    }
                    System.out.println("\n"+num);

                    //make lectime_list for checking cohesion
                    ArrayList<String> lectime_list= new ArrayList<String>();

                    //lectime& lecname show
                    for (lecture lec : f_lecs) {
                        System.out.println(lec.lecname+" "+lec.lectime);
                        String[] lecs_splited=lec.lectime.split(",");
                        for (String piece:lecs_splited){
                            lectime_list.add(piece);
                        }
                    }

                    System.out.print("Not sorted : ");
                    for (String lec : lectime_list){
                        System.out.print(lec);//before sort print
                    }
                    System.out.println();

                    System.out.print("Sorted : ");
                    Collections.sort(lectime_list);//sorted lectime
                    for (String lec:lectime_list){
                        System.out.print(lec);
                    }
                    System.out.println();

                    cohesion=cohesioncheck(lectime_list);
                    System.out.print(cohesion);
                    System.out.println("\n\nDone\n");
                    cohesion_checked_list.put(combinations.indexOf(f_lecs),cohesion);
                    break;

                }else if(compare(lecs.get(i).lectime.split(","),lecs.get(i).lecname)){
                    f_lecs.add(lecs.get(i));
                    total_leccredit+=lecs.get(i).lec_credit;

                }
            }
        }
        System.out.println("unsorted map: " + cohesion_checked_list);
        sorted_map.putAll(cohesion_checked_list);
        System.out.println("results: " + sorted_map);
        // cohesion sort


    }

    //중복검사
    public static boolean duplication_check(ArrayList<lecture> f_lecs){
        boolean result=true;
        for (ArrayList<lecture> combination:combinations) {
            if (new HashSet(combination).equals(new HashSet(f_lecs))) {
                //which combination is duplicated.
                for (lecture comb:combination){
                    System.out.println(comb.lecname+" "+comb.lectime);
                }
                System.out.println();
                for (lecture lec:f_lecs){
                    System.out.println(lec.lecname+" "+lec.lectime);
                }

                System.out.println("\n"+combinations.indexOf(combination)+"is duplicated with this.");
                result=false;//중복 검증
            }else{
                continue;
            }
        }
        return result;
    }
    public static double cohesioncheck(ArrayList<String> lectime){
        //응집도 계산을 위한 함수
        double cohesion_degree=0;//응집도
        ArrayList<Double> m_daytime=new ArrayList<Double>();
        ArrayList<Double> tu_daytime=new ArrayList<Double>();
        ArrayList<Double> w_daytime=new ArrayList<Double>();
        ArrayList<Double> th_daytime=new ArrayList<Double>();
        ArrayList<Double> f_daytime=new ArrayList<Double>();

        for (String lec:lectime){
            String day= String.valueOf(lec.charAt(0));//day check in lectime  ex 월 화 수 목 금
            if (day.equals("월")){
                String type=String.valueOf(lec.charAt(1));//check time lectime abcd 1234567
                m_daytime.add(calc(type));

            }else if (day.equals("화")){
                String type=String.valueOf(lec.charAt(1));//check time lectime abcd 1234567
                tu_daytime.add(calc(type));

            }else if (day.equals("수")){
                String type=String.valueOf(lec.charAt(1));//check time lectime abcd 1234567
                w_daytime.add(calc(type));

            }else if (day.equals("목")){
                String type=String.valueOf(lec.charAt(1));//check time lectime abcd 1234567
                th_daytime.add(calc(type));

            }else if (day.equals("금")){
                String type=String.valueOf(lec.charAt(1));//check time lectime abcd 1234567
                f_daytime.add(calc(type));

            }
        }
        Collections.sort(m_daytime);
        Collections.sort(tu_daytime);
        Collections.sort(w_daytime);
        Collections.sort(th_daytime);
        Collections.sort(f_daytime);
        for (int i=0;i<m_daytime.size()-1;i++){
            cohesion_degree+=m_daytime.get(i+1)-m_daytime.get(i);
        }
        for (int i=0;i<tu_daytime.size()-1;i++){
            cohesion_degree+=tu_daytime.get(i+1)-tu_daytime.get(i);
        }
        for (int i=0;i<w_daytime.size()-1;i++){
            cohesion_degree+=w_daytime.get(i+1)-w_daytime.get(i);
        }
        for (int i=0;i<th_daytime.size()-1;i++){
            cohesion_degree+=th_daytime.get(i+1)-th_daytime.get(i);

        }
        for (int i=0;i<f_daytime.size()-1;i++){
            cohesion_degree+=f_daytime.get(i+1)-f_daytime.get(i);

        }

        return cohesion_degree;

    }
    public static double calc(String type){
        //checking
        double result=0;
        if (isAlpha(type)){
            double itype = type.charAt(0);
            return itype;
        }else if (isNumeric(type)){
            double itype = Double.parseDouble(type);
            double ichange=change(itype);
            return ichange;
        }
        return result;
    }
    public static double change(double time){
        if(time==1 || time==2 || time==3){
            time+=63;
        }else if (time==4){
            time+=62.5;
        }else if (time==5 || time==6){
            time+=62;
        }else if (time==7){
            time+=61.5;
        }else {
            time+=61;
        }
        return time;
    }

    public static boolean isAlpha(String str){
        return str.matches("^[a-zA-Z]*$");
    }
    public static boolean isNumeric(String str){
        return str.matches("^[1-9]*$");
    }
    public static boolean compare(String[] lectime,String lecname){
        for (int i=0;i<f_lecs.size();i++)
            for (int j=0;j<lectime.length;j++){
                if (f_lecs.get(i).lecname.equals(lecname)){
                    return false;
                }else {
                    String day=Character.toString(lectime[j].charAt(0));
                    if (lectime[j].equals(day+"1")) {
                        if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day+"A"))
                            return false;
                    } else if (lectime[j].equals(day+"2")) {
                        if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day+"A"))
                            return false;
                    } else if (lectime[j].equals(day+"3")) {
                        if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day+"B"))
                            return false;
                    } else if (lectime[j].equals(day+"4")) {
                        if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day+"B") || f_lecs.get(i).lectime.contains(day+"C"))
                            return false;
                    } else if (lectime[j].equals(day+"5")) {
                        if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day+"C"))
                            return false;
                    } else if (lectime[j].equals(day+"6")) {
                        if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day+"D"))
                            return false;
                    } else if (lectime[j].equals(day+"7")) {
                        if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day+"D") || f_lecs.get(i).lectime.contains(day+"E"))
                            return false;
                    } else if (lectime[j].equals(day+"8")) {
                        if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day+"E"))
                            return false;
                    } else if (lectime[j].equals(day+"A")) {
                        if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day+"1") || f_lecs.get(i).lectime.contains(day+"2"))
                            return false;
                    } else if (lectime[j].equals(day+"B")) {
                        if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day+"3") || f_lecs.get(i).lectime.contains(day+"4"))
                            return false;
                    } else if (lectime[j].equals(day+"C")) {
                        if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day+"4") || f_lecs.get(i).lectime.contains(day+"5"))
                            return false;
                    } else if (lectime[j].equals(day+"D")) {
                        if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day+"6") || f_lecs.get(i).lectime.contains(day+"7"))
                            return false;
                    } else if (lectime[j].equals(day+"E")) {
                        if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day+"7") || f_lecs.get(i).lectime.contains(day+"8"))
                            return false;
                    }
                }
            }
        return true;
    }
}
class ValueComparator implements Comparator<Integer> {
    Map<Integer, Double> base;

    public ValueComparator(Map<Integer, Double> base) {
        this.base = base;
    }
    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    public int compare(Integer a, Integer b) {
        if (base.get(a) <= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }

}