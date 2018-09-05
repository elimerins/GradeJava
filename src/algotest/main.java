package algotest;

import javax.swing.plaf.nimbus.State;
import java.io.FileReader;
import java.util.*;
import java.sql.*;

class lecturealgo {
    static int total_leccredit = 0;

    //declare fixed timetable array
    static ArrayList<lecture> f_lecs = new ArrayList<lecture>();//출력될 시간표 조합
    static ArrayList<ArrayList<lecture>> combinations = new ArrayList<ArrayList<lecture>>();
    static HashMap<Integer, Double> cohesion_checked_list = new HashMap<Integer, Double>();
    static ArrayList<ArrayList<lecture>> resultList = new ArrayList<ArrayList<lecture>>();
    static ArrayList<lecture> lecs = new ArrayList<lecture>();//시간표들
    static ArrayList<lecture> cd_lecs = new ArrayList<lecture>();//교양 강의
    static ArrayList<lecture> En_lecs = new ArrayList<lecture>();//영어 강의
    static ArrayList<lecture> User_lecs = new ArrayList<lecture>();//영어 강의



    public static void main(String[] args) {
        //declare arraylist
        long start = System.currentTimeMillis();

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

        //전공까지 자동 필터링을 위해서 2 추가해서 배열 선언
        String[] LiberalArtCodes = new String[numberOfLiberalArts + 2];
        for (int i = 0; i < numberOfLiberalArts; i++) {
            //교양 코드 넣기
            System.out.print("Input liberal art code you want to hear : ");
            int LiberalArtCode = n.nextInt();
            LiberalArtCodes[i] = Integer.toString(LiberalArtCode);
        }
        //마지막은 자동으로 전공분류를 위해서 777삽입
        LiberalArtCodes[numberOfLiberalArts] = "777";
        LiberalArtCodes[numberOfLiberalArts + 1] = "666";


        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gtg?serverTimezone=UTC", "gtg", "password");
            //쿼리문에서 대부분 조작가능. 공강, 시간대, professor name등..
            //cor_cd가 null=>전공이므로 오류방지를 위해 임의로 0을 삽입

            st = connection.createStatement();
            String additional_query=
                    "AND (TIME NOT LIKE '%1%')"+
                    "AND (TIME NOT LIKE '%금%') "+
                    "AND (SEMESTER=20) " +
                    "AND (IFNULL(TIME,'') <> '') " +
                    "AND (TITLE NOT LIKE '%(외국인반)%')";

            //전공
            String sql = "SELECT title,time,credit,IFNULL(cor_cd,'777') cor_cd " +
                    "FROM COURSE " +
                    "WHERE ((MAJ_CD in ('CP0116') " +//전공
                    //"AND not TITLE like '종합프로젝트'" +
                    "AND (" +
                    "GRADE LIKE '%1%' " +
                    //" GRADE LIKE '%2%'" +
                    //"or GRADE LIKE '%3%'" +
                    //"or GRADE LIKE '%4%'" +
                    ")" +
                    //"AND TITLE NOT LIKE '%(외국인반)%'" +
                    ")" +
                    "or (cor_cd in (";

            String temp = "";
            for (int i = 0; i < numberOfLiberalArts; i++) {
                temp += ",?";
            }
            temp = temp.replaceFirst(",", "");
            temp += "))";
            sql = sql + temp;

            String sql2 =
                    "AND (TITLE NOT LIKE '%Practic%') " +
                    "AND (TITLE NOT LIKE '%진로세미나%'))";
            sql2+=additional_query;

            PreparedStatement pst = connection.prepareStatement(sql + sql2);

            for (int i = 0; i < numberOfLiberalArts; i++) {
                pst.setString(i + 1, LiberalArtCodes[i]);
            } // for

            ResultSet rs = pst.executeQuery();
            System.out.println(rs);

            while (rs.next()) {
                String time = (String) rs.getObject(2);
                time = time.replaceAll(" ", "");//시간에 들어가는 모든 공백 제거
                lecs.add(new lecture((String) rs.getObject(1), time, (int) rs.getObject(3), (String) rs.getObject(4)));
            }

            //학점이 모자랄 경우 보충해주기 위한 교양 code
            String cd_sql = "SELECT title,time,credit,IFNULL(cor_cd,'777') cor_cd " +
                    "FROM COURSE " +
                    "WHERE " +
                    "COR_CD IN ('101','102','103','104'," +
                    "'105','106','107','108','109'," +
                    "'110','111','112','113','114'," +
                    "'115','116','117','118','119','999') " +
                    "AND title NOT like '%Practic%' "; // 영어를 제외해야하기때문

            cd_sql+=additional_query;
            rs = st.executeQuery(cd_sql);
            System.out.println(rs);

            while (rs.next()) {
                String time = (String) rs.getObject(2);
                time = time.replaceAll(" ", "");//시간에 들어가는 모든 공백 제거
                cd_lecs.add(new lecture((String) rs.getObject(1), time, (int) rs.getObject(3), (String) rs.getObject(4)));
            }

            //영어만
            //특정과목에서도 사용가능
            String En_sql = "SELECT title,time,credit,IF(cor_cd='110','666',cor_cd) " +
                    "FROM COURSE " +
                    "WHERE title like '%English B2%' ";

            En_sql+=additional_query;

            rs = st.executeQuery(En_sql);
            System.out.println(rs);

            while (rs.next()) {
                String time = (String) rs.getObject(2);
                time = time.replaceAll(" ", "");//시간에 들어가는 모든 공백 제거
                En_lecs.add(new lecture((String) rs.getObject(1), time, (int) rs.getObject(3), (String) rs.getObject(4)));
            }
            //특정과목
            /*String User_sql = "SELECT title,time,credit,cor_cd " +
                    "FROM COURSE " +
                    "WHERE title like '%한국사%' " +
                    "AND TIME like '%목1%'" +
                    "AND SEMESTER=20 ";
            User_sql+=additional_query;

            rs = st.executeQuery(User_sql);
            System.out.println(rs);

            while (rs.next()) {
                String time = (String) rs.getObject(2);
                time = time.replaceAll(" ", "");//시간에 들어가는 모든 공백 제거
                User_lecs.add(new lecture((String) rs.getObject(1), time, (int) rs.getObject(3), (String) rs.getObject(4)));
            }*/

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
        System.out.println("TimeTable making START");
        MakeTimeTable(min_credits, max_credits);
        System.out.println("TimeTable making END");

        System.out.println();
        Iterator<ArrayList<lecture>> iter = combinations.iterator();

        //전공, 교양 포함여부 필터링 코드
        while (iter.hasNext()) {
            ArrayList<lecture> flecs = iter.next();
            int result_count = 0;
            for (int i = 0; i < LiberalArtCodes.length; i++) {
                int major_count = 0;
                for (lecture lec : flecs) {
                    if (lec.lec_cd.equals(LiberalArtCodes[i])) {
                        if (LiberalArtCodes[i].equals("777")) {
                            if (major_count == 0) {
                                major_count += 1;
                                //전공이 많이들어가면 count가 계속 누적되어 의미가 없어짐
                            } else {
                                continue;
                            }
                        }
                        result_count += 1;
                    }
                }

            }
            //각 flecs가 사용자가 원하는 교양 코드를 가지고있는지 검증
            if (result_count < LiberalArtCodes.length) {// 1학년은 전공이 없는 경우가있음..
                iter.remove();
            }
        }

        //resultList 내에 각 comb는 순서와 상관없는 독립성을 지니고있어야한다.
        //독립성 검증& resultlist에 최종 삽입
        System.out.println("Checking Duplication");
        for (ArrayList<lecture> comb : combinations) {
            duplication_check(comb);
        }

        //정렬 후 최종 출력
        PrintList(sorted_map);
        long end = System.currentTimeMillis();

        System.out.println("실행 시간 : " + (end - start) / 1000.0);

    }//main

    private static void PrintList(TreeMap<Integer, Double> sorted_map) {
        // cohesion sort
        System.out.println("unsorted map: " + cohesion_checked_list);
        sorted_map.putAll(cohesion_checked_list);
        System.out.println("results: " + sorted_map);

        //키값 insert
        int[] keys = new int[sorted_map.size()];
        double[] cohesion_list = new double[sorted_map.size()];
        for (int i = 0; i < sorted_map.size(); i++) {
            keys[i] = (int) new Vector(sorted_map.keySet()).get(i);
            cohesion_list[i] = (double) new Vector(sorted_map.values()).get(i);

        }
        for (int i = 0; i < 10; i++) {
            if (i > resultList.size()) {
                break;
            }
            System.out.println("=============");
            //System.out.println(keys[i]);
            int credits = 0;

            try {
                for (int j = 0; j < resultList.get(keys[i]).size(); j++) {
                    System.out.println(resultList.get(keys[i]).get(j).lec_cd + " " + resultList.get(keys[i]).get(j).lecname + " " + resultList.get(keys[i]).get(j).lectime);
                    credits += resultList.get(keys[i]).get(j).lec_credit;
                }
                System.out.printf("total credits : %d", credits);
                System.out.println();
                System.out.println(cohesion_list[i]);
                System.out.println("=============");
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println(combinations.size());
            }

        }
    }

    private static boolean MakeTimeTable(int min_credits, int max_credits) {

        for (int num = 0; num < 1000; num++) {
            total_leccredit = 0;
            //섞기
            Collections.shuffle(lecs);
            Collections.shuffle(En_lecs);

            f_lecs.clear();
            //비교를 위한 첫번째 아이템 추가
            for (lecture Userlec:User_lecs){
                if (compare(Userlec.lectime.split(","),Userlec.lecname,Userlec.lec_cd,f_lecs)){
                    f_lecs.add(Userlec);
                }
            }

            for (lecture Enlec:En_lecs){
                    if (compare(Enlec.lectime.split(","),Enlec.lecname,Enlec.lec_cd,f_lecs)) {
                        f_lecs.add(Enlec);
                        break;
                    }

            }
            if (f_lecs.size()==0){
                f_lecs.add(lecs.get(0));
            }

            for (lecture flecs:f_lecs){
                total_leccredit += flecs.lec_credit;
            }


            //시간표 후보군(from db) 속에서 1개의 시간표 조합 출력
            if(!MakeCombination(lecs, max_credits, min_credits)){
                MakeCombination(cd_lecs, max_credits, min_credits);
            }
        }
        return true;
    }

    private static boolean MakeCombination(ArrayList<lecture> lecs, int max_credits, int min_credits) {
        boolean result=false;
        for (int i = 1; i < lecs.size(); i++) {
            //조건에 맞는 최소 학점에 도달
            if (total_leccredit < max_credits + 1) {
                if (total_leccredit < min_credits) {
                    if (compare(lecs.get(i).lectime.split(","), lecs.get(i).lecname, lecs.get(i).lec_cd, f_lecs)) {
                        f_lecs.add(lecs.get(i));
                        total_leccredit += lecs.get(i).lec_credit;
                    }
                } else if (total_leccredit >= min_credits) {
                    //not over max_credit and over min credit
                    //deep copy. shallow copy는 clear시 연결되어 같이 초기화됨.
                    //combinations에 현재 시간표 조합 f_lecs를 추가.
                    //이 부분은 티스토리에 같이 적는 것이 좋겠다.

                    //중복 검사를 패스한 시간표는 학점이 남을 수 있어서 combinations에 add
                    combinations.add((ArrayList<lecture>) f_lecs.clone());
                    result=true;
                    if (compare(lecs.get(i).lectime.split(","), lecs.get(i).lecname, lecs.get(i).lec_cd, f_lecs)) {
                        f_lecs.add(lecs.get(i));
                        total_leccredit += lecs.get(i).lec_credit;
                    } else {
                        break;
                    }
                }
            } else if (total_leccredit == max_credits) {
                combinations.add((ArrayList<lecture>) f_lecs.clone());
                result=true;
                break;
            } else {
                break;
            }

        }
        return result;
    }

    //중복검사 메소드
    private static boolean duplication_check(ArrayList<lecture> comb) {
        boolean result = true;
        //combinations 리스트에서 검색
        //which combination is duplicated.
        //조합 순서에 상관없이 내용물이 같다면,
        for (ArrayList<lecture> OneOfresult : resultList) {

            if (new HashSet(comb).equals(new HashSet(OneOfresult))) {
                //print if comb is duplicate with OneOfresult
                return false;
            }
        }

        ArrayList<lecture> comb2 = (ArrayList<lecture>) comb.clone();
        resultList.add(comb2);

        //make lectime_list for checking cohesion
        //각 시간표의 응집도 계산을위한 lectime_list 생성
        ArrayList<String> lectime_list = new ArrayList<String>();

        //lectime& lecname show
        for (lecture lec : comb2) {
            //System.out.println(lec.lecname+" "+lec.lectime);
            String[] lecs_splited = lec.lectime.split(",");
            for (String piece : lecs_splited) {
                lectime_list.add(piece);
            }
        }

        //sorted lectime
        //시간을 정렬하고 정렬된 시간표를 보여줌
        Collections.sort(lectime_list);

        double cohesion = cohesioncheck(lectime_list, comb2);

        //시간표에 대한 인덱스와 시간표의 응집도를 cohesion check list 에 저장
        cohesion_checked_list.put(resultList.indexOf(comb2), cohesion);

        return result;
    }//duplication_check

    //응집도 계산을 위한 함수
    private static double cohesioncheck(ArrayList<String> lectime, ArrayList<lecture> comb2) {
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
        if (lectime.size() >= 7) {
            return cohesion_degree;
        }
        if (cohesion_degree <= 1.0 && lectime.size() >= 6.0) {
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

    public static boolean compare(String[] lectime, String lecname, String lec_cd, ArrayList<lecture> f_lecs) {
        //코드겹침 여부 계산 함수
        if (!lec_cd.equals("777")) {
            for (int i = 0; i < f_lecs.size(); i++) {
                if (f_lecs.get(i).lec_cd.equals(lec_cd)) {
                    return false;
                }
            }
        }
        //시간겹침 여부 계산함수
        for (int i = 0; i < f_lecs.size(); i++)
            if (f_lecs.get(i).lecname.contains(lecname)) {
                return false;
            } else if (lecname.contains(f_lecs.get(i).lecname)) {
                return false;
            } else if (lecname.equals(f_lecs.get(i).lecname)) {
                return false;
            } else {
                for (int j = 0; j < lectime.length; j++) {
                    try {
                        //시간표 후보의 강의 요일 추출
                        String day = null;
                        try {
                            day = Character.toString(lectime[j].charAt(0));
                        } catch (StringIndexOutOfBoundsException e) {
                            System.out.println(lecname);
                            return false;
                        }
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
                        } else if (lectime[j].equals(day + "9")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]))
                                return false;
                        } else if (lectime[j].equals(day + "10")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]))
                                return false;
                        } else if (lectime[j].equals(day + "11")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]))
                                return false;
                        } else if (lectime[j].equals(day + "12")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]))
                                return false;
                        } else if (lectime[j].equals(day + "13")) {
                            if (f_lecs.get(i).lectime.contains(lectime[j]))
                                return false;
                        } else if (lectime[j].equals(day + "A")) {
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