import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.File;
import java.util.concurrent.atomic.AtomicReference;


public class MovieAnalyzer {

    public List<List<String>> datas = new ArrayList<>();

    public String noEmpty(String s) {
        StringBuilder ans = new StringBuilder();
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) != ' ')
                ans.append(s.charAt(i));
        return ans.toString();
    }

    public String noComma(String s) {
        StringBuilder ans = new StringBuilder();
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) != ',')
                ans.append(s.charAt(i));
        return ans.toString();
    }

    public List<String> splitLine(String line) {
//		line = line + ',';
        List<String> ans = new ArrayList<>();
        int flag = 0;
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if ((c == ',') && flag == 0) {
                if (s.length() > 0 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
                    s.deleteCharAt(s.length() - 1);
                    s.deleteCharAt(0);
                }
                ans.add(s.toString());
                s = new StringBuilder();
            } else {
                s.append(c);
                if (c == '"')
                    flag ^= 1;
                if (i == line.length() - 1) {
                    s.deleteCharAt(s.length() - 1);
                    s.deleteCharAt(0);
                    ans.add(noComma(s.toString()));
                }
            }

        }
//		System.out.println(ans.toArray().length);
        return ans;
    }

    public MovieAnalyzer(String dataset_path) throws IOException {
        Scanner scanner = new Scanner(new File(dataset_path), StandardCharsets.UTF_8);
//		int flag = 0;
		System.out.println(scanner.nextLine());
        while (scanner.hasNext()) {
            List<String> data = splitLine(scanner.nextLine());
            this.datas.add(data);
        }
        scanner.close();
    }

    public Map<Integer, Integer> getMovieCountByYear() {
        Map<Integer, Integer> ans = new LinkedHashMap<>();
        for (List<String> item : this.datas) {
            String year = item.get(2);
            if (year.equals(""))
                continue;
            Integer ce = Integer.parseInt(year);
            if (ans.containsKey(ce))
                ans.replace(ce, ans.get(ce), ans.get(ce) + 1);
            else
                ans.put(ce, 1);
        }
        List<Map.Entry<Integer, Integer>> ce = new ArrayList<>(ans.entrySet());
        ce.sort((a, b) -> -1 * Integer.compare(a.getKey(), b.getKey()));
        Map<Integer, Integer> kk = new LinkedHashMap<>();
        ce.forEach(o -> kk.put(o.getKey(), o.getValue()));
        return kk;
    }


    public Map<String, Integer> getMovieCountByGenre() {
        Map<String, Integer> ans = new LinkedHashMap<>();
        for (List<String> item : this.datas) {
            String genre = item.get(5);
            if (genre.equals(""))
                continue;
            String[] genreInItem = genre.split(",");
            for (String eachGenre : genreInItem) {
                genre = noEmpty(eachGenre);
                if (ans.containsKey(genre))
                    ans.replace(genre, ans.get(genre), ans.get(genre) + 1);
                else
                    ans.put(genre, 1);
            }
        }
        List<Map.Entry<String, Integer>> answer = new ArrayList<>(ans.entrySet());
        answer.sort((a, b) ->
                (a.getValue().equals(b.getValue()) ? a.getKey().compareTo(b.getKey()) :
                        Integer.compare(-a.getValue(), -b.getValue())));
        Map<String, Integer> kk = new LinkedHashMap<>();
        answer.forEach(x -> kk.put(x.getKey(), x.getValue()));
        return kk;
    }

    public List<String> getName(String a, String b) {
        List<String> ans = new ArrayList<>();
        if (a.compareTo(b) < 0) {
            ans.add(a);
            ans.add(b);
        } else {
            ans.add(b);
            ans.add(a);
        }
        return ans;
    }

    public void addStars(Map<List<String>, Integer> map, String a, String b) {
        List<String> ce = getName(a, b);
        if (map.containsKey(ce))
            map.replace(ce, map.get(ce), map.get(ce) + 1);
        else
            map.put(ce, 1);
    }

    public Map<List<String>, Integer> getCoStarCount() {
        Map<List<String>, Integer> map = new LinkedHashMap<>();
        for (List<String> item : this.datas) {
            String s1 = item.get(10);
            String s2 = item.get(11);
            String s3 = item.get(12);
            String s4 = item.get(13);
            if (s1.equals("") || s2.equals("") || s3.equals("") || s4.equals(""))
                continue;
            addStars(map, s1, s2);
            addStars(map, s1, s3);
            addStars(map, s1, s4);
            addStars(map, s2, s3);
            addStars(map, s2, s4);
            addStars(map, s3, s4);
        }
        return map;
    }

    public List<String> getTopMovies(int top_k, String by) {
        int num = by.equals("runtime") ? 4 : 7;
        List<Map.Entry<Integer, String>> ans = new ArrayList<>();
        for (List<String> item : this.datas) {
            if (item.get(num).equals("") || item.get(1).equals(""))
                continue;
            int time = 0;
            if (num == 4) {
//				System.out.println(item.get(num).substring(0, item.get(num).length() - 4));
                time = Integer.parseInt(item.get(num).substring(0, item.get(num).length() - 4));
            } else
                time = item.get(num).length();
            String title = item.get(1);
            ans.add(Map.entry(time, title));
        }
        ans.sort(
                (a, b) -> a.getKey().equals(b.getKey()) ? a.getValue().compareTo(b.getValue()) : Integer.compare(-a.getKey(), -b.getKey())
        );
        List<String> kk = new ArrayList<>();
//		ans.forEach(o -> kk.add(o.getValue()));
        for (int i = 0; i < top_k; i++)
            kk.add(ans.get(i).getValue());
        return kk;
    }

    public void addRating(Map<String, List<Float>> map, String star, Float rate) {
        if (map.containsKey(star)) {
            map.get(star).add(rate);
        } else {
            List<Float> ce = new ArrayList<>();
            ce.add(rate);
            map.put(star, ce);
        }
    }

    public List<String> getTopStars(int top_k, String by) {
        Map<String, List<Float>> starRating = new LinkedHashMap<>();
        int num = by.equals("rating") ? 6 : 15;
        for (List<String> item : this.datas) {
            if (item.size() == 15 && num == 15)
                continue;
            String s1 = item.get(10);
            String s2 = item.get(11);
            String s3 = item.get(12);
            String s4 = item.get(13);
//			System.out.println(item.size());
            if (item.get(num).equals(""))
                continue;
            if (s1.equals("") || s2.equals("") || s3.equals("") || s4.equals(""))
                continue;
            addRating(starRating, s1, Float.parseFloat(item.get(num)));
            addRating(starRating, s2, Float.parseFloat(item.get(num)));
            addRating(starRating, s3, Float.parseFloat(item.get(num)));
            addRating(starRating, s4, Float.parseFloat(item.get(num)));
        }
        List <Map.Entry<String, List<Float>>> ans = new ArrayList<>(starRating.entrySet());
        ans.sort((a, b) ->
        {
            AtomicReference<Double> s1 = new AtomicReference<>(0.0);
            AtomicReference<Double> s2 = new AtomicReference<>(0.0);
            a.getValue().forEach(x -> s1.updateAndGet(v -> v + x));
            b.getValue().forEach(x -> s2.updateAndGet(v -> v + x));
            s1.updateAndGet(v -> v * b.getValue().size());
            s2.updateAndGet(v -> v * a.getValue().size());
            return s1.get().equals(s2.get()) ? a.getKey().compareTo(b.getKey()): Double.compare(-s1.get(), -s2.get());
        });
        List<String> kk = new ArrayList<>();
        for (int i = 0; i < top_k; i++) {
            kk.add(ans.get(i).getKey());
            AtomicReference<Double> sum = new AtomicReference<>(0.0);
            ans.get(i).getValue().forEach(x -> sum.updateAndGet(v -> v + x));
//			System.out.println(ans.get(i).getKey());
//			System.out.println(sum.get() / ans.get(i).getValue().size());
        }
//		ans.forEach(x -> kk.add(x.getKey()));
        return kk;
    }

    public List<String> searchMovies(String genre, float min_rating, int max_runtime) {
        List<String> ans = new ArrayList<>();
        for (List<String> item : this.datas) {
            if (item.get(5).equals("") || item.get(6).equals("") || item.get(4).equals("") || item.get(1).equals(""))
                continue;
            if (item.get(5).contains(genre) && Double.parseDouble(item.get(6)) >= min_rating &&
                    Integer.parseInt(item.get(4).substring(0, item.get(4).length() - 4)) <= max_runtime)
                ans.add(item.get(1));
        }
        ans.sort(String::compareTo);
        return ans;
    }

//	public static void main(String[] args) throws Exception {
//		MovieAnalyzer movieAnalyzer = new MovieAnalyzer("resources/imdb_top_500.csv");
//		System.out.println(movieAnalyzer.datas.get(0).toString());
//		System.out.println(movieAnalyzer.getMovieCountByYear().toString());
//		System.out.println(movieAnalyzer.getMovieCountByGenre().toString());
//		System.out.println(movieAnalyzer.getTopMovies(10, "runtime"));
//		System.out.println(movieAnalyzer.getTopMovies(10, "overview"));
//		System.out.println(movieAnalyzer.getTopStars(10, "rating"));
//		System.out.println(movieAnalyzer.getTopStars(10, "gross"));
//		System.out.println(movieAnalyzer.searchMovies("Drama", 0.0F, 120));
////		System.out.println(movieAnalyzer.getCoStarCount().toString());
//
//	}


}
