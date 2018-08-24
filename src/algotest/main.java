package algotest;

import java.util.*;
import java.sql.*;

class lecturealgo {
    static int total_leccredit = 0;

    //declare fixed timetable array
    static ArrayList<lecture> f_lecs = new ArrayList<lecture>();//출력될 시간표 조합
    static ArrayList<ArrayList<lecture>> combinations = new ArrayList<ArrayList<lecture>>();
    static HashMap<Integer, Double> cohesion_checked_list = new HashMap<Integer, Double>();
    static ArrayList<ArrayList<lecture>> resultList =new ArrayList<ArrayList<lecture>>();
    static ArrayList<lecture> lecs = new ArrayList<lecture>();//시간표들
    static int title_cd[]={010,011,012,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,999};

    public static void main(String[] args) {
        //declare arraylist

        ValueComparator bvc = new ValueComparator(cohesion_checked_list);
        TreeMap<Integer, Double> sorted_map = new TreeMap<Integer, Double>(bvc);

        Connection connection = null;
        Statement st = null;

        Scanner n = new Scanner(System.in);
        System.out.print("How many credits do you want to hear at least? : ");
        int min_credits = n.nextInt();
        System.out.print("How many for MAX? : ");
        int max_credits = n.nextInt();

        //libereal arts categorize.
        System.out.print("How many kinds of liberal arts do you want to hear? : ");
        int numberOfLiberalArts = n.nextInt();

        //전공까지 자동 필터링을 위해서 1 추가해서 배열 선언
        int[] LiberalArtCodes=new int[numberOfLiberalArts+1];
        for (int i=0;i<LiberalArtCodes.length-1;i++){
            //교양 코드 넣기
            System.out.print("Input liberal art code you want to hear : ");
            int LiberalArtCode=n.nextInt();
            LiberalArtCodes[i]=LiberalArtCode;
        }
        //마지막은 자동으로 전공분류를 위해서 0삽입
        LiberalArtCodes[numberOfLiberalArts]=0;

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gtg?serverTimezone=UTC", "gtg", "password");
            st = connection.createStatement();
            //쿼리문에서 대부분 조작가능. 공강, 시간대, professor name등..
            //cor_cd가 null=>전공이므로 오류방지를 위해 임의로 0을 삽입
            String sql = "SELECT title,time,credit,IFNULL(cor_cd,'0') cor_cd " +
                    "FROM COURSE " +
                    "WHERE (((MAJ_CD in (select maj_cd " +
                                    "from mj " +
                                    "where MAJ_CD='C20110'))"+
                    "AND SEMESTER=20 " +
                    //"AND not TITLE like '종합프로젝트'" +
                    "AND (" +
                    //"GRADE LIKE '%3%' " +
                    //"or GRADE LIKE '%4%'" +
                    "GRADE LIKE '%2%'" +
                    "or GRADE LIKE '%3%'" +
                    "))" +
                    "or cor_cd in('101','103','104','102','105','106','107','108','109','110','112','113','114') " +
                    ")" +
                    //"AND not time like '%금%'" +
                    "AND IFNULL(TIME, '') <> '' " +
                    "AND NOT TITLE LIKE '%(외국인반)%'";


            ResultSet rs = st.executeQuery(sql);
            System.out.println(rs);

            while (rs.next()) {
                String time = (String) rs.getObject(2);
                time = time.replaceAll(" ", "");//시간에 들어가는 모든 공백 제거
                lecs.add(new lecture((String) rs.getObject(1), time, (int) rs.getObject(3),(String) rs.getObject(4)));
            }
            rs.close();
            st.close();
            connection.close();
        } catch (SQLException se1) {
            se1.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (st != null)
                    st.close();
            } catch (SQLException se2) {
            }
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        //n개의 시간표 조합 작성
        MakeTimeTable(lecs,min_credits,max_credits);

        System.out.println();

        //전공, 교양 포함여부 필터링 코드
        for (int i=0;i<LiberalArtCodes.length;i++){
            for (int j=0;j<combinations.size();j++){
                try {
                    //조건을 만족하지 못하면 해당 시간표 조합 combinations에서 remove
                    //교양 및 전공 코드를 가지고 있는지 확인
                    if(!Conditional_remove(combinations.get(j),LiberalArtCodes[i])){
                        combinations.remove(combinations.get(j));
                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    break;
                }

            }
        }

        //resultList 내에 각 comb는 순서와 상관없는 독립성을 지니고있어야한다.
        //독립성 검증& resultlist에 최종 삽입
        for (ArrayList<lecture> comb:combinations){
            duplication_check(comb);
        }

        // cohesion sort
        System.out.println("unsorted map: " + cohesion_checked_list);
        sorted_map.putAll(cohesion_checked_list);
        System.out.println("results: " + sorted_map);

        //키값 insert
        int[] keys= new int[sorted_map.size()];
        double[] cohesion_list= new double[sorted_map.size()];
        for(int i=0;i<sorted_map.size();i++)
        {
            keys[i]= (int) new Vector(sorted_map.keySet()).get(i);
            cohesion_list[i]=(double) new Vector(sorted_map.values()).get(i);
            /*System.out.printf("%d : %f",new Vector(sorted_map.keySet()).get(i),new Vector(sorted_map.values()).get(i));
            System.out.println();
            if(i>=5){
                break;
            }*/
        }
        /*for (int key:keys){
            System.out.println(key);
            for (int i=0;i<combinations.get(key).size();i++) {
                System.out.println(combinations.get(key).get(i).lecname +" "+combinations.get(key).get(i).lectime);
            }
            System.out.println("-------");

        }*/
        ArrayList<String> lectime_list2 = new ArrayList<String>();
        //정렬 후 최종 출력
        for (int i=0;i<3;i++){
            if(i>resultList.size()){
                break;
            }
            System.out.println("=============");
            //System.out.println(keys[i]);
            int credits=0;
            for (int j=0;j<resultList.get(keys[i]).size();j++){
                System.out.println(resultList.get(keys[i]).get(j).lecname +" "+resultList.get(keys[i]).get(j).lectime+" "+resultList.get(keys[i]).get(j).lec_cd);
                credits+=resultList.get(keys[i]).get(j).lec_credit;
            }
            System.out.printf("total credits : %d",credits);
            System.out.println();
            System.out.println(cohesion_list[i]);
            System.out.println("=============");

        }
        /*for (lecture lec : combinations.get(keys[i])) {
            //System.out.println(lec.lecname+" "+lec.lectime);
            String[] lecs_splited=lec.lectime.split(",");
            for (String piece:lecs_splited){
                lectime_list2.add(piece);
            }
        }
        Collections.sort(lectime_list2);
        double cohesion2 = cohesioncheck(lectime_list2,combinations.get(keys[i]));
        System.out.println(cohesion2);
        */


    }//main

    private  static boolean MakeTimeTable(ArrayList<lecture> lecs,int min_credits,int max_credits){
        for (int num = 0; num < 500; num++) {
            total_leccredit = 0;
            //섞기
            Collections.shuffle(lecs);

            f_lecs.clear();
            //비교를 위한 첫번째 아이템 추가
            f_lecs.add(lecs.get(0));
            total_leccredit += f_lecs.get(0).lec_credit;

            //시간표 후보군(from db) 속에서 1개의 시간표 조합 출력
            for (int i = 1; i < lecs.size(); i++) {
                //조건에 맞는 최소 학점에 도달
                if (total_leccredit<max_credits+1) {
                    if(total_leccredit<min_credits){
                        if (compare(lecs.get(i).lectime.split(","), lecs.get(i).lecname,lecs.get(i).lec_cd,f_lecs)){
                            f_lecs.add(lecs.get(i));
                            total_leccredit += lecs.get(i).lec_credit;
                        }
                    }else if(total_leccredit>=min_credits) {
                        //not over max_credit and over min credit
                        AddCombinationList(f_lecs,num);
                        if (compare(lecs.get(i).lectime.split(","), lecs.get(i).lecname,lecs.get(i).lec_cd,f_lecs)) {
                            f_lecs.add(lecs.get(i));
                            total_leccredit += lecs.get(i).lec_credit;
                        }else{
                            break;
                        }
                    }
                } else if (total_leccredit==max_credits){
                    AddCombinationList(f_lecs,num);
                    break;
                }else{
                    break;
                }
            }
        }
        return true;
    }
    private static boolean Conditional_remove(ArrayList<lecture> f_lecs,int code){
        for (int i=0;i<f_lecs.size();i++){
            if (Integer.parseInt(f_lecs.get(i).lec_cd)==code){
                return true;
            }
        }
        return false;
    }

    private static boolean AddCombinationList(ArrayList<lecture> f_lecs,int num){


        //deep copy. shallow copy는 clear시 연결되어 같이 초기화됨.
        //combinations에 현재 시간표 조합 f_lecs를 추가.
        //이 부분은 티스토리에 같이 적는 것이 좋겠다.

        //중복 검사를 패스한 시간표는 학점이 남을 수 있어서 combinations에 add
        combinations.add((ArrayList<lecture>) f_lecs.clone());

        //lectime& lecname show
        //1개의 시간표 대한 시간을 ',' 기준으로 쪼개서 배열로 나누고 리스트에 1 요소씩 추가
        /*System.out.println("\n"+num);
        for (lecture lec : f_lecs) {
            System.out.println(lec.lecname + " " + lec.lectime);

        }
        System.out.println(total_leccredit);*/


        return true;

    }

    //중복검사 메소드
    private static boolean duplication_check(ArrayList<lecture> comb) {
        boolean result = true;
        //combinations 리스트에서 검색
        //which combination is duplicated.
        //조합 순서에 상관없이 내용물이 같다면,


        for (ArrayList<lecture> OneOfresult:resultList){

            if (new HashSet(comb).equals(new HashSet(OneOfresult))){
                //print if comb is duplicate with OneOfresult
                /*System.out.println(IndexOf+" of combinations is duplicated with " +
                        ""+resultList.indexOf(OneOfresult)+
                        " of resultList");*/
                return false;
            }
        }
        /*
        동일성 확인을 위한 comb의 sort 체크
        Collections.sort(comb, new Comparator<lecture>() {
            @Override
            public int compare(lecture lec1, lecture lec2) {
                return lec1.lecname.compareToIgnoreCase(lec2.lecname);
            }
        });
        */
        ArrayList<lecture> comb2=(ArrayList<lecture>) comb.clone();
        resultList.add(comb2);

        //make lectime_list for checking cohesion
        //각 시간표의 응집도 계산을위한 lectime_list 생성
        ArrayList<String> lectime_list = new ArrayList<String>();

        //lectime& lecname show
        for (lecture lec : comb2) {
            //System.out.println(lec.lecname+" "+lec.lectime);
            String[] lecs_splited=lec.lectime.split(",");
            for (String piece:lecs_splited){
                lectime_list.add(piece);
            }
        }
        //정렬되지않은 시간을 보여줌
        /*System.out.print("Not sorted : ");
        for (String lec : lectime_list) {
            System.out.print(lec);//before sort print
        }
        System.out.println();*/


        //sorted lectime
        //시간을 정렬하고 정렬된 시간표를 보여줌
        Collections.sort(lectime_list);
        /*System.out.print("Sorted : ");
        for (String lec : lectime_list) {
            System.out.print(lec);
        }
        System.out.println();*/

        double cohesion = cohesioncheck(lectime_list,comb2);
        //System.out.print(cohesion);
        //System.out.println("\n\nDone\n");
        /*if (cohesion==0.0){
            System.out.println(resultList.indexOf(comb2)+" "+lectime_list);
        }*/

        //시간표에 대한 인덱스와 시간표의 응집도를 cohesion check list 에 저장
        cohesion_checked_list.put(resultList.indexOf(comb2), cohesion);

        return result;
    }//duplication_check

    //응집도 계산을 위한 함수
    private static double cohesioncheck(ArrayList<String> lectime,ArrayList<lecture> comb2) {

        double cohesion_degree = 0.0;//응집도
        //요일간 시간표 리스트
        ArrayList<Double> m_daytime = new ArrayList<Double>();
        ArrayList<Double> tu_daytime = new ArrayList<Double>();
        ArrayList<Double> w_daytime = new ArrayList<Double>();
        ArrayList<Double> th_daytime = new ArrayList<Double>();
        ArrayList<Double> f_daytime = new ArrayList<Double>();

        for (String lec : lectime) {
            //요일마다 시간표리스트 추가
            String day = String.valueOf(lec.charAt(0));//day check in lectime  ex 월 화 수 목 금
            if (day.equals("월")) {
                String type = String.valueOf(lec.charAt(1));//check time lectime abcd 1234567
                m_daytime.add(calc(type));

            } else if (day.equals("화")) {
                String type = String.valueOf(lec.charAt(1));//check time lectime abcd 1234567
                tu_daytime.add(calc(type));

            } else if (day.equals("수")) {
                String type = String.valueOf(lec.charAt(1));//check time lectime abcd 1234567
                w_daytime.add(calc(type));

            } else if (day.equals("목")) {
                String type = String.valueOf(lec.charAt(1));//check time lectime abcd 1234567
                th_daytime.add(calc(type));

            } else if (day.equals("금")) {
                String type = String.valueOf(lec.charAt(1));//check time lectime abcd 1234567
                f_daytime.add(calc(type));

            }
        }
        Collections.sort(m_daytime);
        Collections.sort(tu_daytime);
        Collections.sort(w_daytime);
        Collections.sort(th_daytime);
        Collections.sort(f_daytime);
        for (int i = 0; i < m_daytime.size() - 1; i++) {
            cohesion_degree += m_daytime.get(i + 1) - m_daytime.get(i);
        }
        for (int i = 0; i < tu_daytime.size() - 1; i++) {
            cohesion_degree += tu_daytime.get(i + 1) - tu_daytime.get(i);
        }
        for (int i = 0; i < w_daytime.size() - 1; i++) {
            cohesion_degree += w_daytime.get(i + 1) - w_daytime.get(i);
        }
        for (int i = 0; i < th_daytime.size() - 1; i++) {
            cohesion_degree += th_daytime.get(i + 1) - th_daytime.get(i);
        }
        for (int i = 0; i < f_daytime.size() - 1; i++) {
            cohesion_degree += f_daytime.get(i + 1) - f_daytime.get(i);

        }
        if (lectime.size()>=7){
            return cohesion_degree;
        }
        if (cohesion_degree<=1.0 && lectime.size()>=6.0){
            return cohesion_degree;
        }

        return cohesion_degree;

    }

    //isAlpha? or Numeric?
    private static double calc(String type) {

        double result = 0;
        if (isAlpha(type)) {
            double itype = type.charAt(0);//아스키 코드값으로 변환
            return itype;
        } else if (isNumeric(type)) {
            double itype = Double.parseDouble(type);//알파벳 교시와 맞춤
            double ichange = change(itype);
            return ichange;
        }
        return result;
    }

    //numeric class change to ascii code
    private static double change(double time) {

        if (time == 1 || time == 2 || time == 3) {
            time += 63;
        } else if (time == 4) {
            time += 62.5;
        } else if (time == 5 || time == 6) {
            time += 62;
        } else if (time == 7) {
            time += 61.5;
        } else {
            time += 61;
        }
        return time;
    }

    public static boolean isAlpha(String str) {
        return str.matches("^[a-zA-Z]*$");
    }

    public static boolean isNumeric(String str) {
        return str.matches("^[1-9]*$");
    }

    public static boolean compare(String[] lectime, String lecname,String lec_cd,ArrayList<lecture> f_lecs) {
        //시간겹침,코드겹침계산을 위한 함수
        for (int i=0;i<f_lecs.size();i++){
            if (f_lecs.get(i).lec_cd.equals(lec_cd)){
                return false;
            }
        }

        for (int i = 0; i < f_lecs.size(); i++)
            for (int j = 0; j < lectime.length; j++) {
                if (f_lecs.get(i).lecname.contains(lecname)) {
                    return false;
                } else if(lecname.contains(f_lecs.get(i).lecname)){
                    return false;
                } else {
                    try {
                        //시간표 후보의 강의 요일 추출
                        String day = Character.toString(lectime[j].charAt(0));
                        if (lectime[j].equals(day + "1")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day + "A"))
                                return false;
                        } else if (lectime[j].equals(day + "2")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day + "A"))
                                return false;
                        } else if (lectime[j].equals(day + "3")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day + "B"))
                                return false;
                        } else if (lectime[j].equals(day + "4")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day + "B") || f_lecs.get(i).lectime.contains(day + "C"))
                                return false;
                        } else if (lectime[j].equals(day + "5")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day + "C"))
                                return false;
                        } else if (lectime[j].equals(day + "6")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day + "D"))
                                return false;
                        } else if (lectime[j].equals(day + "7")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day + "D") || f_lecs.get(i).lectime.contains(day + "E"))
                                return false;
                        } else if (lectime[j].equals(day + "8")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day + "E"))
                                return false;
                        }else if (lectime[j].equals(day + "9")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]) )
                                return false;
                        }else if (lectime[j].equals(day + "10")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j])  )
                                return false;
                        }else if (lectime[j].equals(day + "11")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j])  )
                                return false;
                        }else if (lectime[j].equals(day + "12")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]) )
                                return false;
                        }else if (lectime[j].equals(day + "13")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j])  )
                                return false;
                        }else if (lectime[j].equals(day + "A")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day + "1") || f_lecs.get(i).lectime.contains(day + "2"))
                                return false;
                        } else if (lectime[j].equals(day + "B")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day + "3") || f_lecs.get(i).lectime.contains(day + "4"))
                                return false;
                        } else if (lectime[j].equals(day + "C")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day + "4") || f_lecs.get(i).lectime.contains(day + "5"))
                                return false;
                        } else if (lectime[j].equals(day + "D")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day + "6") || f_lecs.get(i).lectime.contains(day + "7"))
                                return false;
                        } else if (lectime[j].equals(day + "E")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]) || f_lecs.get(i).lectime.contains(day + "7") || f_lecs.get(i).lectime.contains(day + "8"))
                                return false;
                        }
                    } catch (StringIndexOutOfBoundsException e) {
                        System.out.println(lectime[j]);
                        System.err.printf("%nException : %s%n", e);
                        e.printStackTrace();
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