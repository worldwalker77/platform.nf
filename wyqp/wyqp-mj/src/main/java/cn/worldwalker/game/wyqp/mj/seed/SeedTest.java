package cn.worldwalker.game.wyqp.mj.seed;

public class SeedTest {



    static SeedService seedService = SeedService.getInstance();
    static SeedService seedServiceFeng = SeedService.getInstanceFeng();

    public static void main(String[] arsgs){
//        TableMgr.getInstance().load();

        seedService.printInfo();
        seedServiceFeng.printInfo();

       for (int i=0; i<=4; i++){
           System.out.println();
           System.out.println("laiziCnt:" + i);
           System.out.println("noGen-----");
//           compare(seedService.getSeeds(false,i), TableMgr.getInstance().m_check_table[i]);
           System.out.println("gen-----");
//           compare(seedService.getSeeds(true,i), TableMgr.getInstance().m_check_eye_table[i]);
           System.out.println("noGenFeng-----");
//           compare(seedServiceFeng.getSeeds(false,i), TableMgr.getInstance().m_check_feng_table[i]);
           System.out.println("genFeng-----");
//           compare(seedServiceFeng.getSeeds(true,i), TableMgr.getInstance().m_check_feng_eye_table[i]);
       }

    }

    private static Seed trans(Integer integer){
        int val = integer;
        int i = 0;
        int[] seed = new int[9];
        while (val > 0){
            seed[i] = val%10;
            val = val/10;
            i++;
        }
        return new Seed(seed);
    }

    private static Integer trans(Seed seed){
        int[] seeds = seed.getSeed();
        Integer val = 0;
        for (int i=seeds.length-1; i > -1; i--){
            val = val*10 + seeds[i];
        }
        return val;
    }


    /*
    private static void compare(Set<Seed> seeds, SetTable setTable){

        for (Seed seed : seeds){
            Integer val = trans(seed);
            if (!setTable.m_tbl.keySet().contains(val)){
                System.out.println("not in " + seed);
            }
        }

        Set<Integer> set = new HashSet<>(256);
        for (Integer integer : setTable.m_tbl.keySet()){
            if (!seeds.contains(trans(integer))){
                set.add(integer);
//                System.out.println(integer);
            }
        }
        System.out.println(  set.size()
                + " ," + setTable.m_tbl.size() + " ," + set.size() /(double)setTable.m_tbl.size());
    }
    */
}
