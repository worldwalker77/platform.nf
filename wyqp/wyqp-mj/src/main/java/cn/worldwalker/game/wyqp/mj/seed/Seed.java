package cn.worldwalker.game.wyqp.mj.seed;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Seed {
    private int[] seed ;

    public Seed(int[] seed) {
        this.seed = seed;
    }

    public int[] getSeed() {
        return seed;
    }

    public void add(Seed seed){
        for (int i=0; i<this.seed.length; i++){
            this.seed[i] = this.seed[i] + seed.seed[i];
        }
    }

    public int sum(){
        int sum = 0;
        for (int aSeed : this.seed) {
            sum += aSeed;
        }
        return sum;
    }

    public boolean isIllegal(){
        for (int aSeed : this.seed){
            if (aSeed> 4){
                return false;
            }
        }
        return true;
    }

    public Set<Seed> generateLaizi(){
        Set<Seed> seedList = new HashSet<>(16);
        for (int i=0; i<seed.length; i++){
            int[] newSeed = new int[9];
            System.arraycopy(seed, 0, newSeed, 0, 9);
            Seed seedLaizi = new Seed(newSeed);
            if (seedLaizi.seed[i]> 0){
                seedLaizi.seed[i] = seedLaizi.seed[i] - 1;
                seedList.add(seedLaizi);
            }
        }
        return seedList;
    }

    @Override
    public String toString() {
        return Arrays.toString(seed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Seed seed1 = (Seed) o;

        return Arrays.equals(seed, seed1.seed);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(seed);
    }
}
