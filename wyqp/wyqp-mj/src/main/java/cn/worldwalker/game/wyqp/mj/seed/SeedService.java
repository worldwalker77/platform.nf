package cn.worldwalker.game.wyqp.mj.seed;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SeedService {
    private static SeedService ourInstance = new SeedService(false);

    private static SeedService ourInstanceFeng = new SeedService(true);

    public static SeedService getInstance() {
        return ourInstance;
    }

    public static SeedService getInstanceFeng() {
        return ourInstanceFeng;
    }

    private SeedService(boolean isFeng) {
        this.isFeng = isFeng;
        long curTime = System.currentTimeMillis();
        init();
        initGen();
        initLaizi();
        System.out.println("costTime: " + (System.currentTimeMillis() - curTime));
    }

    private boolean isFeng;

    private static final int MAX_GEN_CNT = 1;

    private Map<Integer, Set<Seed>> mapSeed = new HashMap<>(4);
    private Map<Integer, Set<Seed>> mapSeedGen = new HashMap<>(4);

    public void printInfo() {
        printInfo(mapSeed, "noGen");
        printInfo(mapSeedGen, "withGen");
    }

    public Set<Seed> getSeeds(boolean withGen, int laiziCnt) {
        return withGen ? mapSeedGen.get(laiziCnt) : mapSeed.get(laiziCnt);
    }

    private void init() {
        Set<Seed> oneSeeds = new HashSet<>(16);

        if (!isFeng) {
            oneSeeds.add(new Seed(new int[]{1, 1, 1, 0, 0, 0, 0, 0, 0}));
            oneSeeds.add(new Seed(new int[]{0, 1, 1, 1, 0, 0, 0, 0, 0}));
            oneSeeds.add(new Seed(new int[]{0, 0, 1, 1, 1, 0, 0, 0, 0}));
            oneSeeds.add(new Seed(new int[]{0, 0, 0, 1, 1, 1, 0, 0, 0}));
            oneSeeds.add(new Seed(new int[]{0, 0, 0, 0, 1, 1, 1, 0, 0}));
            oneSeeds.add(new Seed(new int[]{0, 0, 0, 0, 0, 1, 1, 1, 0}));
            oneSeeds.add(new Seed(new int[]{0, 0, 0, 0, 0, 0, 1, 1, 1}));
        }else {
            //东南西北
            oneSeeds.add(new Seed(new int[]{0, 1, 1, 1, 0, 0, 0, 0, 0}));
            oneSeeds.add(new Seed(new int[]{1, 0, 1, 1, 0, 0, 0, 0, 0}));
            oneSeeds.add(new Seed(new int[]{1, 1, 0, 1, 0, 0, 0, 0, 0}));
            oneSeeds.add(new Seed(new int[]{1, 1, 1, 0, 0, 0, 0, 0, 0}));
            //中发白
            oneSeeds.add(new Seed(new int[]{0, 0, 0, 0, 1, 1, 1, 0, 0}));
        }
        oneSeeds.add(new Seed(new int[]{3, 0, 0, 0, 0, 0, 0, 0, 0}));
        oneSeeds.add(new Seed(new int[]{0, 3, 0, 0, 0, 0, 0, 0, 0}));
        oneSeeds.add(new Seed(new int[]{0, 0, 3, 0, 0, 0, 0, 0, 0}));
        oneSeeds.add(new Seed(new int[]{0, 0, 0, 3, 0, 0, 0, 0, 0}));
        oneSeeds.add(new Seed(new int[]{0, 0, 0, 0, 3, 0, 0, 0, 0}));
        oneSeeds.add(new Seed(new int[]{0, 0, 0, 0, 0, 3, 0, 0, 0}));
        oneSeeds.add(new Seed(new int[]{0, 0, 0, 0, 0, 0, 3, 0, 0}));
        if (!isFeng){
            oneSeeds.add(new Seed(new int[]{0, 0, 0, 0, 0, 0, 0, 3, 0}));
            oneSeeds.add(new Seed(new int[]{0, 0, 0, 0, 0, 0, 0, 0, 3}));
        }


        Set<Seed> twoSeeds = seedCombine(oneSeeds, oneSeeds);
        Set<Seed> threeSeeds = seedCombine(twoSeeds, oneSeeds);
        Set<Seed> fourSeeds = seedCombine(threeSeeds, oneSeeds);

        Set<Seed> allSeeds = new HashSet<>(1 << 12);
        allSeeds.addAll(oneSeeds);
        allSeeds.addAll(twoSeeds);
        allSeeds.addAll(threeSeeds);
        allSeeds.addAll(fourSeeds);

        mapSeed.put(0, allSeeds);
    }

    private void initGen() {
        Set<Seed> headList = new HashSet<>(16);
        headList.add(new Seed(new int[]{2, 0, 0, 0, 0, 0, 0, 0, 0}));
        headList.add(new Seed(new int[]{0, 2, 0, 0, 0, 0, 0, 0, 0}));
        headList.add(new Seed(new int[]{0, 0, 2, 0, 0, 0, 0, 0, 0}));
        headList.add(new Seed(new int[]{0, 0, 0, 2, 0, 0, 0, 0, 0}));
        headList.add(new Seed(new int[]{0, 0, 0, 0, 2, 0, 0, 0, 0}));
        headList.add(new Seed(new int[]{0, 0, 0, 0, 0, 2, 0, 0, 0}));
        headList.add(new Seed(new int[]{0, 0, 0, 0, 0, 0, 2, 0, 0}));
        if (!isFeng){
            headList.add(new Seed(new int[]{0, 0, 0, 0, 0, 0, 0, 2, 0}));
            headList.add(new Seed(new int[]{0, 0, 0, 0, 0, 0, 0, 0, 2}));
        }

        Set<Seed> allSeedsGen = seedCombine(mapSeed.get(0), headList);
        allSeedsGen.addAll(headList);

        mapSeedGen.put(0, allSeedsGen);

    }

    private void initLaizi() {
        generateLaziSeeds(mapSeed);
        generateLaziSeeds(mapSeedGen);
    }

    private void generateLaziSeeds(Map<Integer, Set<Seed>> mapSeed) {
        Set<Seed> initSeeds = mapSeed.get(0);
        for (int i = 1; i <= MAX_GEN_CNT; i++) {
            Set<Seed> set = new HashSet<>(1 << 20);
            for (Seed seed1 : initSeeds) {
                set.addAll(seed1.generateLaizi());
            }
            mapSeed.put(i, set);
            initSeeds = set;
        }
    }

    private void printInfo(Map<Integer, Set<Seed>> mapSeed, String mapName) {
        for (Map.Entry<Integer, Set<Seed>> entry : mapSeed.entrySet()) {
            System.out.println(mapName + " -- gen:" + entry.getKey() + ", seeds:" + entry.getValue().size());
        }
    }

    private Set<Seed> seedCombine(Set<Seed> set1, Set<Seed> set2) {
        Set<Seed> seedSet = new HashSet<>(1 << 20);
        for (Seed seed1 : set1) {
            for (Seed seed2 : set2) {
                Seed seed = new Seed(new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0});
                seed.add(seed1);
                seed.add(seed2);
                if (seed.isIllegal() && seed.sum() <= 14)
                    seedSet.add(seed);
            }
        }
        return seedSet;
    }
}
