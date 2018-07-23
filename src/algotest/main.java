package algotest;

import java.util.*;
import java.sql.*;

class lecturealgo {
    static int total_leccredit = 0;

    //declare fixed timetable array
    static ArrayList<lecture> f_lecs = new ArrayList<lecture>();//출력될 시간표 조합
    static ArrayList<ArrayList<lecture>> combinations = new ArrayList<ArrayList<lecture>>();

    public static void main(String[] args) {
        //declare arraylist
        ArrayList<lecture> lecs = new ArrayList<lecture>();//시간표들
        HashMap<Integer, Double> cohesion_checked_list = new HashMap<Integer, Double>();
        ValueComparator bvc = new ValueComparator(cohesion_checked_list);
        TreeMap<Integer, Double> sorted_map = new TreeMap<Integer, Double>(bvc);

        Connection connection = null;
        Statement st = null;

        Scanner n = new Scanner(System.in);
        System.out.print("How many credits do you want to hear at least? : ");
        int credits = n.nextInt();
        System.out.print("How many for MAX? : ");
        int max_credits = n.nextInt();

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gtg?serverTimezone=UTC", "gtg", "password");
            st = connection.createStatement();
            //쿼리문에서 대부분 조작가능. 공강, 시간대, professor name등..
            String sql = "select title,time,credit" +
                    " FROM course " +
                    "WHERE grade like '%컴퓨터공학과%' " +
                    "and not time like '' " +
                    "and semester=20 " +
                    "and maj_cd like 'CJ0200' " +
                    "and not time like '월%'" ;

            ResultSet rs = st.executeQuery(sql);
            System.out.println(rs);

            ResultSetMetaData metaData = rs.getMetaData();
            int numberOfColumns = metaData.getColumnCount();
            while (rs.next()) {
                String time = (String) rs.getObject(2);
                time = time.replaceAll(" ", "");//시간에 들어가는 모든 공백 제거
                lecs.add(new lecture((String) rs.getObject(1), time, (int) rs.getObject(3)));
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

        //시간표 조합 출력
        for (int num = 0; num < 5; num++) {
            double cohesion = 0.0;
            total_leccredit = 0;
            Collections.shuffle(lecs);
            f_lecs.clear();
            f_lecs.add(lecs.get(0));
            total_leccredit += f_lecs.get(0).lec_credit;


            for (int i = 1; i < lecs.size(); i++) {//시간표 후보군(from db) 속에서 조합시작

                if (credits <= total_leccredit && total_leccredit <= max_credits) {// 범위설정

                    if (!duplication_check(f_lecs)) {//과목들의 중복검사
                        System.out.println(num + " is duplicated.\n");
                        break;
                    } else {
                        //deep copy. shallow copy는 clear시 연결되어 같이 초기화됨.
                        combinations.add((ArrayList<lecture>) f_lecs.clone());//이 부분은 티스토리에 같이 적는 것이 좋겠다.
                    }
                    System.out.println("\n" + num);

                    //make lectime_list for checking cohesion
                    ArrayList<String> lectime_list = new ArrayList<String>();

                    //lectime& lecname show
                    for (lecture lec : f_lecs) {
                        System.out.println(lec.lecname + " " + lec.lectime);
                        String[] lecs_splited = lec.lectime.split(",");
                        for (String piece : lecs_splited) {
                            lectime_list.add(piece);
                        }
                    }
                    System.out.println(total_leccredit);

                    System.out.print("Not sorted : ");
                    for (String lec : lectime_list) {
                        System.out.print(lec);//before sort print
                    }
                    System.out.println();

                    System.out.print("Sorted : ");
                    Collections.sort(lectime_list);//sorted lectime
                    for (String lec : lectime_list) {
                        System.out.print(lec);
                    }
                    System.out.println();

                    cohesion = cohesioncheck(lectime_list);
                    System.out.print(cohesion);
                    System.out.println("\n\nDone\n");
                    cohesion_checked_list.put(combinations.indexOf(f_lecs), cohesion);
                    break;

                } else if (compare(lecs.get(i).lectime.split(","), lecs.get(i).lecname)) {
                    //범위에 도달하지 못했다면 add.
                    f_lecs.add(lecs.get(i));
                    total_leccredit += lecs.get(i).lec_credit;

                }
            }
        }
        System.out.println("unsorted map: " + cohesion_checked_list);
        sorted_map.putAll(cohesion_checked_list);
        System.out.println("results: " + sorted_map);
        // cohesion sort


    }//main

    //중복검사 메소드
    private static boolean duplication_check(ArrayList<lecture> f_lecs) {
        boolean result = true;
        for (ArrayList<lecture> combination : combinations) {

            if (new HashSet(combination).equals(new HashSet(f_lecs))) {
                //which combination is duplicated.

                System.out.println("--------BREAK-------");

                for (lecture lec : f_lecs) {
                    System.out.println(lec.lecname + " " + lec.lectime);
                }
                System.out.println();
                for (lecture comb : combination) {
                    System.out.println(comb.lecname + " " + comb.lectime);
                }

                System.out.println("\n" + combinations.indexOf(combination) + " is duplicated with this.");
                result = false;//중복 검증
            } else {
                continue;
            }
        }
        return result;
    }//duplication_check

    //응집도 계산을 위한 함수
    private static double cohesioncheck(ArrayList<String> lectime) {

        double cohesion_degree = 0;//응집도
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

    public static boolean compare(String[] lectime, String lecname) {
        //시간겹침계산을 위한 함수
        for (int i = 0; i < f_lecs.size(); i++)
            for (int j = 0; j < lectime.length; j++) {
                if (f_lecs.get(i).lecname.equals(lecname)) {
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