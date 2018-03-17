package cn.worldwalker.game.wyqp.mj.robot;

import cn.worldwalker.game.wyqp.mj.huvalidate.Hulib;
import cn.worldwalker.game.wyqp.mj.huvalidate.TableMgr;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HuServiceTest {
    @Test
    public void testIsHu() throws Exception {
        TableMgr.getInstance().load();
        Map<List<Integer>,Boolean> caseMap = new HashMap<>(16);
        caseMap.put(Arrays.asList(0,1,2,3,4,5,6,7,8), true);
        //常规牌型
        caseMap.put(Arrays.asList(0,1,2,3,4,5,6,7,8,10,11,12), true);
        caseMap.put(Arrays.asList(0,1,2,3,4,5,6,7,8,10,11,12,13,13), true);
        caseMap.put(Arrays.asList(1,1,1,3,4,5,6,7,8,10,11,12), true);
        caseMap.put(Arrays.asList(1,1,1,3,4,5,6,7,8,10,11,12,13,13), true);
        //万与筒交接处
        caseMap.put(Arrays.asList(8,9,10), false);
        //筒与条交接处
        caseMap.put(Arrays.asList(16,17,18), false);
        //条与风交接处
        caseMap.put(Arrays.asList(25,26,27), false);
        //东南西北任意三个
        caseMap.put(Arrays.asList(1,1,27,28,29), true);
        caseMap.put(Arrays.asList(0,0,28,29,30), true);
        caseMap.put(Arrays.asList(2,2,27,29,30), true);
        caseMap.put(Arrays.asList(3,3,27,28,30), true);
        //中发白
        caseMap.put(Arrays.asList(3,3,31,32,33), true);
        //风 碰子
        caseMap.put(Arrays.asList(0,0,27,27,27), true);
        //十三烂
        caseMap.put(Arrays.asList(0,3,6,9,12,15,18,21,24,29,30,31,32,33), true);
        //七对
        caseMap.put(Arrays.asList(0,0,1,1,8,8,9,9,11,11,18,18,33,33), true);



        for (Map.Entry<List<Integer>,Boolean> entry : caseMap.entrySet()){
            //无赖子
            boolean isHuNew = HuService.getInstance().isHu(entry.getKey());
            boolean isHuOld = Hulib.getInstance().get_hu_info(entry.getKey(), 100,100,34);
            if (!entry.getValue().equals(isHuNew)){
                HuService.getInstance().isHu(entry.getKey());
                System.out.println(entry.getKey());
            }
            Assert.assertTrue(entry.getValue().equals(isHuNew));
            if (isHuNew != isHuOld){
                System.out.println(entry);
            }
        }
    }


}