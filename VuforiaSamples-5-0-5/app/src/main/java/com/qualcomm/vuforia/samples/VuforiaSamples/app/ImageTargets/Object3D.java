package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;


import android.graphics.CornerPathEffect;

import com.qualcomm.vuforia.samples.SampleApplication.utils.CubeObject;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuesongwang on 11/29/15.
 */
public class Object3D {
    public static int SCALE;

    public CubeObject cube;
    public List<int[]> offsetList;
    private double length, width, height;
    public int centerX;
    public int centerY;
    public int centerZ;
    public boolean isSticked;
    public boolean lock;
    public float[] selfRotationMatrix;
    public int textureId;
    public int interval;

    public Object3D(int x, int y, int z,int t,List<int[]>c) {
        this.centerX = x;
        this.centerY = y;
        this.centerZ = z;
        this.offsetList = c;
        this.textureId =t;
        cube = new CubeObject();
    }

    private Map<Coordinate2D,Integer> findLowestBottom()
    {
        HashMap<Coordinate2D,Integer> coordinateAndZMap = new HashMap<>();
        for(int[] offsetXYZ: offsetList) {
            Coordinate2D coord = new Coordinate2D(centerX+ offsetXYZ[0],centerY + offsetXYZ[1]);
            if(!coordinateAndZMap.containsKey(coord))
                coordinateAndZMap.put(coord,offsetXYZ[2]);
            else {
                if (offsetXYZ[2] < coordinateAndZMap.get(coord))
                    coordinateAndZMap.put(coord, offsetXYZ[2]);
            }
        }
        return coordinateAndZMap;
    }

    public static void main(String[] args)
    {
        Object3D o = new Object3D(0,0,0,8,new ArrayList<int[]>());
        ArrayList<int[]> arrayList = new ArrayList<>();
        arrayList.add(new int[]{1,1,1});
        arrayList.add(new int[]{1,1,-1});
        arrayList.add(new int[]{-1,1,1});
        arrayList.add(new int[]{-1,1,2});
        arrayList.add(new int[]{0,0,0});

        o.offsetList = arrayList;
        Map<Coordinate2D,Integer>  a = o.findLowestBottom();
        for(Coordinate2D key:a.keySet())
        {
            System.out.println(a.get(key));
        }
    }

    private static HashMap<Integer,boolean[][]> levelOccupation;

    public static class Coordinate2D{
        @Override
        public boolean equals(Object o) {
            Coordinate2D c = (Coordinate2D)o;
            return (c.X == this.X) && (c.Y == this.Y);
        }

        @Override
        public int hashCode() {
            return X * 100000 + Y;
        }

        public Coordinate2D(int x, int y) {
            X = x;
            Y = y;
        }

        public int X;
        public int Y;
    }

}
