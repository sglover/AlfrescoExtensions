/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services.minhash;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * Adapted from https://github.com/thomasjungblut/thomasjungblut-common
 * 
 * @param <T>
 */
public class MinHashImpl<T> implements MinHash<T>
{
	private int hash[];
	private int numHash;

	/**
	 * 
	 */
	public MinHashImpl(int numHash){
		this.numHash = numHash;
        hash = new int[numHash];

        Random r = new Random(11);
        for (int i = 0; i < numHash; i++){
            int a = (int)r.nextInt();
            int b = (int)r.nextInt();
            int c = (int)r.nextInt();
            int x = hash(a*b*c, a, b, c);
            hash[i] = x;
        } 
    }
	

    public double similarity(Set<T> set1, Set<T> set2){

        int numSets = 2;
        Map<T, boolean[]> bitMap = buildBitMap(set1, set2);
        
        int[][] minHashValues = initializeHashBuckets(numSets, numHash);

        computeMinHashForSet(set1, 0, minHashValues, bitMap);
        computeMinHashForSet(set2, 1, minHashValues, bitMap);

        return computeSimilarityFromSignatures(minHashValues, numHash);
    }
    
	/**
	 * 
	 */
	private static int[][] initializeHashBuckets(int numSets, int numHashFunctions) {
		int[][] minHashValues = new int[numSets][numHashFunctions];

        for (int i = 0; i < numSets; i++) {
        	for (int j = 0; j < numHashFunctions; j++) {
        		minHashValues[i][j] = Integer.MAX_VALUE;
            }
        }
        return minHashValues;
    }
	 
	/**
	 * 
	 * @param minHashValues
	 * @param numHashFunctions
	 * @return
	 */
	private static double computeSimilarityFromSignatures(int[][] minHashValues, int numHashFunctions) {
		int identicalMinHashes = 0;
        for (int i = 0; i < numHashFunctions; i++){
            if (minHashValues[0][i] == minHashValues[1][i]) {
                identicalMinHashes++;
            }
        }
        return (1.0 * identicalMinHashes) / numHashFunctions;
    }
	
	/**
	 * 
	 * @param x
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	private static int hash(int x, int a, int b, int c) {
        int hashValue = (int)((a * (x >> 4) + b * x + c) & 131071);
        return Math.abs(hashValue);
    }
	
	
    private void computeMinHashForSet(Set<T> set, int setIndex, int[][] minHashValues, Map<T, boolean[]> bitArray){

    	int index=0;
    	
    	for(T element : bitArray.keySet()) { // for every element in the bit array
    		for (int i = 0; i < numHash; i++){ // for every hash
    			if(set.contains(element)) { // if the set contains the element
    				int hindex = hash[index]; // get the hash
    				if (hindex < minHashValues[setIndex][index]) { 
    					// if current hash is smaller than the existing hash in the slot then replace with the smaller hash value
    					minHashValues[setIndex][i] = hindex;
    				}
    			}
    		}
    		index++;
    	}
    }
    
	/**
	 * 
	 * @param set1
	 * @param set2
	 * @return
	 */
	public Map<T,boolean[]> buildBitMap(Set<T> set1, Set<T> set2){
		
		Map<T,boolean[]> bitArray = new HashMap<T,boolean[]>();
		
		for(T t : set1){
			bitArray.put(t, new boolean[]{true,false});
		}
		
		for(T t : set2){
			if(bitArray.containsKey(t)){
				// item is not present in set1
				bitArray.put(t, new boolean[]{true,true});
			}else if(!bitArray.containsKey(t)){
				// item is not present in set1
				bitArray.put(t, new boolean[]{false,true});
			}
		}
		
		
		return bitArray;
	}
}