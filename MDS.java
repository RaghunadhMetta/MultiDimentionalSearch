


package cs6301.g11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// created required dataStructures required for implementation
public class MDS {
	private LinkedHashMap<Long, Long[]> itemMap; 											// map to store the item id and descriptions
	private LinkedHashMap<Long, Float> supplierMap; 											// map to store suppliers and their reputations
	private HashMap<Long, HashMap<Long, ItemPriceRelation>> supplierToItemMap; 				// map to create a relation between
																							// supplier and item along with the
																							// reputations and prices
	private HashMap<Long, HashSet<Long>> itemToSupplier; 										// map to store the relation between item and corresponding
																							// suppliers.
	private HashMap<Long, HashSet<Long>> itemCountMap; 										// map that stores the occurrence of each word in description and
																							// the items in which it occurred.
	// MDS constructor to initialize

	public MDS() {
		itemMap = new LinkedHashMap<>();
		supplierMap = new LinkedHashMap<>();
		supplierToItemMap = new HashMap<>();
		itemToSupplier = new HashMap<>();
		itemCountMap = new HashMap<>();
	}

	// class to store item and corresponding price
	public static class Pair {
		long id;
		int price;

		public Pair(long id, int price) {
			this.id = id;
			this.price = price;
		}
	}

	// class to store the relation between item and the seller reputation.
	private static class ItemPriceRelation {
		int price;
		float reputation;

		public ItemPriceRelation(int price, float reputation) {
			super();
			this.price = price;
			this.reputation = reputation;
		}
	}

	/*
	 * add a new item. If an entry with the same id already exists, the new
	 * description is merged with the existing description of the item. Returns true
	 * if the item is new, and false otherwise.
	 */

	public boolean add(Long id, Long[] description) {
		Long[] descriptionArray = new Long[description.length];	
		System.arraycopy(description, 0, descriptionArray, 0, description.length);
		for (Long word : descriptionArray) { // update the map that stores the words of description and occurrence of them in
											// the items
			if (itemCountMap.containsKey(word)) {
				HashSet<Long> set = itemCountMap.get(word);
					set.add(id);
					itemCountMap.put(word, set);
			} else {
				HashSet<Long> set = new HashSet<>();
				set.add(id);
				itemCountMap.put(word, set);
			}

		}
		if (!itemMap.containsKey(id)) {				//update the itemmap with the given key and description
			itemMap.put(id, descriptionArray);
			return true;
		} else {		
			Long[] desc = itemMap.get(id);
			Long[] mergedArray = new Long[desc.length + descriptionArray.length];
			System.arraycopy(desc, 0, mergedArray, 0, desc.length);
			System.arraycopy(descriptionArray, 0, descriptionArray, desc.length, descriptionArray.length);
			itemMap.put(id, mergedArray);
			return false;
		}

	}

	/*
	 * add a new supplier (Long) and their reputation (float in [0.0-5.0], single
	 * decimal place). If the supplier exists, their reputation is replaced by the
	 * new value. Return true if the supplier is new, and false otherwise.
	 */
	public boolean add(Long supplier, float reputation) {
		if (supplierToItemMap.containsKey(supplier)) {							//update the supplier item relation map with the given entry
			HashMap<Long, ItemPriceRelation> list = supplierToItemMap.get(supplier);
			for (Map.Entry<Long, ItemPriceRelation> e : list.entrySet()) {
				e.getValue().reputation = reputation;
			}
		}
		if (!supplierMap.containsKey(supplier)) {								//update the supplier map 
			supplierMap.put(supplier, reputation);
			return true;
		} else {
			supplierMap.put(supplier, reputation);
			return false;
		}
	}

	/*
	 * add products and their prices at which the supplier sells the product. If
	 * there is an entry for the price of an id by the same supplier, then the price
	 * is replaced by the new price. Returns the number of new entries created.
	 */
	public int add(Long supplier, Pair[] idPrice) {								//update the supplier item relation map with the given entry
		float reputation =0.0f;
		//count to store the number of new entries added to our map.
		int count = 0;							
		Pair newPair[] = new Pair[idPrice.length];
		System.arraycopy(idPrice, 0, newPair, 0, idPrice.length);
		if (supplierMap.containsKey(supplier)) {
			reputation = supplierMap.get(supplier);
		}
		if (supplierToItemMap.containsKey(supplier)) {
			HashMap<Long, ItemPriceRelation> map = supplierToItemMap.get(supplier);
			for (Pair pair : newPair) {
				if (itemToSupplier.containsKey(pair.id)) {
					HashSet<Long> vendorId = itemToSupplier.get(pair.id);
					vendorId.add(supplier);
					itemToSupplier.put(pair.id, vendorId);
				} else {
					HashSet<Long> set = new HashSet<>();
					set.add(supplier);
					itemToSupplier.put(pair.id, set);
				}
				if (map.containsKey(pair.id)) {
					ItemPriceRelation ipr = map.get(pair.id);
					if (ipr.price != pair.price) {
						ipr.price = pair.price;
						count++;
						map.put(pair.id, ipr);
						supplierToItemMap.put(supplier, map);
					}

				} else {
					ItemPriceRelation ipr = new ItemPriceRelation(pair.price, reputation);
					map.put(pair.id, ipr);
					supplierToItemMap.put(supplier, map);
					count++;
				}

			}

		} else {
			HashMap<Long, ItemPriceRelation> map = new HashMap<>();
			for (Pair pair : newPair) {
				if (itemToSupplier.containsKey(pair.id)) {
					HashSet<Long> vendorId = itemToSupplier.get(pair.id);
					vendorId.add(supplier);
					itemToSupplier.put(pair.id, vendorId);
				} else {
					HashSet<Long> set = new HashSet<>();
					set.add(supplier);
					itemToSupplier.put(pair.id, set);
				}
				ItemPriceRelation ipr = new ItemPriceRelation(pair.price, reputation);
				map.put(pair.id, ipr);
				supplierToItemMap.put(supplier, map);
				count++;
			}
		}
		return count;
	}

	/*
	 * return an array with the description of id. Return null if there is no item
	 * with this id.
	 */
	public Long[] description(Long id) {
		return itemMap.get(id);
	}

	/*
	 * given an array of Longs, return an array of items whose description contains
	 * one or more elements of the array, sorted by the number of elements of the
	 * array that are in the item's description (non-increasing order).
	 */
	
	public Long[] findItem(Long[] arr) {
		//with the given arr use the word to item map and retrieve all the items containing the words
		Map<Long, Integer> map = new HashMap<>();
		for (Long num : arr) {
			if (itemCountMap.containsKey(num)) {
				HashSet<Long> set = itemCountMap.get(num);
				for (Long s : set) {
					if (map.containsKey(s)) {
						int count = map.get(s);
						count = count + 1;
						map.put(s, count);
					} else {
						map.put(s, 1);
					}
				}
			}
		}
		Long array[] = new Long[map.size()];
		int i = 0;
		//sort the map based on the values as per the output requirement  
		Map<Long, Integer> sortedMap = sortByValues(map, true);
		for (Map.Entry<Long, Integer> e : sortedMap.entrySet()) {
			array[i] = (Long) e.getKey();
			i++;
		}
		return array;
	}
//costimized comparator so as to enable sorting by value with order of sorting set throught boolean 
	public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map, boolean nonIncOrder) {
		Comparator<K> valueComparator = new Comparator<K>() {
			public int compare(K k1, K k2) {
				int compare = map.get(k1).compareTo(map.get(k2));
				if (compare == 0)
					return 1;
				else if (nonIncOrder) {
					return -compare;
				} else {
					return compare;
				}
			}
		};

		Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
		sortedByValues.putAll(map);
		return sortedByValues;
	}

	/*
	 * given a Long n, return an array of items whose description contains n, which
	 * have one or more suppliers whose reputation meets or exceeds the given
	 * minimum reputation, that sell that item at a price that falls within the
	 * price range [minPrice, maxPrice] given. Items should be sorted in order of
	 * their minimum price charged by a supplier for that item (non-decreasing
	 * order).
	 */
	public Long[] findItem(Long n, int minPrice, int maxPrice, float minReputation) {
		HashMap<Long, Long> map = new HashMap<>();
		Map<Long, Integer> treemap = new HashMap<>();
		//obtain all the suppliers which sell items that contain 'n'
		if (itemCountMap.containsKey(n)) {
			HashSet<Long> set = itemCountMap.get(n);
			for (Long item : set) {
				if (itemToSupplier.containsKey(item)) {
					HashSet<Long> itemSupplierRelation = itemToSupplier.get(item);
					for (Long l : itemSupplierRelation) {
						map.put(item, l);
					}
				}
			}
		}
		//check the price and reputation constraints and get the final answer
		for (Map.Entry<Long, Long> entry : map.entrySet()) {
			Long vendorId = entry.getValue();
			if (supplierToItemMap.containsKey(vendorId)) {
				HashMap<Long, ItemPriceRelation> priceReputationMap = supplierToItemMap.get(vendorId);
				if (priceReputationMap.containsKey(entry.getKey())) {
					ItemPriceRelation ipr = priceReputationMap.get(entry.getKey());
					if (ipr.price >= minPrice && ipr.price <= maxPrice && ipr.reputation >= minReputation) {
						treemap.put(entry.getKey(), ipr.price);
					}
				}
			}

		}
//sort the map based on value(price)
		Map<Long, Integer> sortedMap = sortByValues(treemap, false);
		Long[] array = new Long[treemap.size()];
		int i = 0;
		for (Map.Entry<Long, Integer> e : sortedMap.entrySet()) {
			array[i] = e.getKey();
			i++;
		}

		return array;
	}

	/*
	 * given an id, return an array of suppliers who sell that item, ordered by the
	 * price at which they sell the item (non-decreasing order).
	 */
	public Long[] findSupplier(Long id) {
		//get the suppliers list directly from the pre organized map
		TreeMap<Long, Integer> treeMap = new TreeMap<>();
		if (itemToSupplier.containsKey(id)) {
			HashSet<Long> set = itemToSupplier.get(id);
			for (Long l : set) {
				if (supplierToItemMap.containsKey(l)) {
					HashMap<Long, ItemPriceRelation> map = supplierToItemMap.get(l);
					if (map.containsKey(id)) {
						ItemPriceRelation ipr = map.get(id);
						treeMap.put(l, ipr.price);
					}
				}
			}
		}
		Long[] array = new Long[treeMap.size()];
		int i = 0;
		//sort the answer based on price with the help of customized comparator
		Map<Long, Integer> sortedMap = sortByValues(treeMap, false);
		for (Map.Entry<Long, Integer> e : sortedMap.entrySet()) {
			array[i] = e.getKey();
			i++;
		}
		return array;
	}

	/*
	 * given an id and a minimum reputation, return an array of suppliers who sell
	 * that item, whose reputation meets or exceeds the given reputation. The array
	 * should be ordered by the price at which they sell the item (non-decreasing
	 * order).
	 */
	
	public Long[] findSupplier(Long id, float minReputation) {
		TreeMap<Long,Integer> treeMap = new TreeMap<>();
		if (itemToSupplier.containsKey(id)) {
			HashSet<Long> set = itemToSupplier.get(id);
			for (Long l : set) {
				if (supplierToItemMap.containsKey(l)) {
					HashMap<Long, ItemPriceRelation> map = supplierToItemMap.get(l);
					if (map.containsKey(id)) {
						ItemPriceRelation ipr = map.get(id);
						if (ipr.reputation >= minReputation) {
							treeMap.put(l,ipr.price);
						}
					}
				}
			}
		}
		Long[] array = new Long[treeMap.size()];
		int i = 0;
		//sort the answer based on price with the help of customized comparator
				Map<Long, Integer> sortedMap = sortByValues(treeMap, false);
				for (Map.Entry<Long, Integer> e : sortedMap.entrySet()) {
					array[i] = e.getKey();
					i++;
				}
		return array;
	}

	/*
	 * find suppliers selling 5 or more products, who have the same identical
	 * profile as another supplier: same reputation, and, sell the same set of
	 * products, at identical prices. This is a rare operation, so do not do
	 * additional work in the other operations so that this operation is fast.
	 * Creative solutions that are elegant and efficient will be awarded excellence
	 * credit. Return array of suppliers satisfying above condition. Make sure that
	 * each supplier appears only once in the returned array.
	 */
	public Long[] identical() {
		
		return null;
	}

	/*
	 * given an array of ids, find the total price of those items, if those items
	 * were purchased at the lowest prices, but only from sellers meeting or
	 * exceeding the given minimum reputation. Each item can be purchased from a
	 * different seller.
	 */
	public int invoice(Long[] arr, float minReputation) {
		int total = 0;

		for (Long item : arr) {

			if (itemToSupplier.containsKey(item)) {
				int tempSum = Integer.MAX_VALUE;
				HashSet<Long> set = itemToSupplier.get(item);
				for (Long l : set) {
					if (supplierToItemMap.containsKey(l)) {
						HashMap<Long, ItemPriceRelation> map = supplierToItemMap.get(l);
						if (map.containsKey(item)) {
							ItemPriceRelation ipr = map.get(item);
							if (ipr.reputation >= minReputation) {
								tempSum = Math.min(tempSum, ipr.price);

							}
						}
					}
				}
				if (tempSum != Integer.MAX_VALUE) {
					total = total + tempSum;
				}
			}

		}
		return total;
	}

	/*
	 * remove all items, all of whose suppliers have a reputation that is equal or
	 * lower than the given maximum reputation. Returns an array with the items
	 * removed.
	 */
	public Long[] purge(float maxReputation) {
		
		List<Long> removedItems = new ArrayList<>();
		for(Map.Entry<Long, HashSet<Long>> itemSupplier : itemToSupplier.entrySet()) {
			boolean qualifiedToRemove = true;
			HashSet<Long> suppliersOfItem = itemSupplier.getValue();
			for(Long supplier : suppliersOfItem) {
				if(supplierMap.containsKey(supplier)) {
					Float rep = supplierMap.get(supplier);
					if(rep>maxReputation) {
						qualifiedToRemove=false;
						break;
					}
					
				}
			}
			if(qualifiedToRemove) {
				removedItems.add(itemSupplier.getKey());
			}
		}
		for (Long item : removedItems) {
			Long[] desc = itemMap.remove(item);
			if (itemToSupplier.containsKey(item)) {
				HashSet<Long> suppliersList = itemToSupplier.remove(item);
				for (Long s : suppliersList) {
					if (supplierToItemMap.containsKey(s)) {
						HashMap<Long, ItemPriceRelation> map = supplierToItemMap.get(s);
						map.remove(item);
						supplierToItemMap.put(s, map);
					}
				}
			}
			for (Long d : desc) {
				if (itemCountMap.containsKey(d)) {
					HashSet<Long> wordPresentItems = itemCountMap.get(d);
					wordPresentItems.remove(item);
					itemCountMap.put(d, wordPresentItems);
				}
			}
		}
		Long[] arr = removedItems.toArray(new Long[removedItems.size()]);
		return arr;
	}

	/*
	 * remove item from storage. Returns the sum of the Longs that are in the
	 * description of the item deleted (or 0, if such an id did not exist).
	 */
	public Long remove(Long id) {
		//removed item has to be updated in the item decs map
		if (itemMap.containsKey(id)) {
			Long[] desc = itemMap.remove(id);
			Long sum = 0L;
			//description word map update
			for (Long d : desc) {
				sum = sum + d;
				if (itemCountMap.containsKey(d)) {
					HashSet<Long> wordPresentItems = itemCountMap.get(d);
					wordPresentItems.remove(id);
					itemCountMap.put(d, wordPresentItems);
				}
			}
			//item and list of suppliers update
			if (itemToSupplier.containsKey(id)) {
				HashSet<Long> suppliersList = itemToSupplier.remove(id);
				//supplier and items with reputation price map update
				for (Long s : suppliersList) {
					if (supplierToItemMap.containsKey(s)) {
						HashMap<Long, ItemPriceRelation> map = supplierToItemMap.get(s);
						map.remove(id);
						supplierToItemMap.put(s, map);
					}
				}
			}

			return sum;
		} else {
			return 0L;
		}
	}

	/*
	 * remove from the given id's description those elements that are in the given
	 * array. It is possible that some elements of the array are not part of the
	 * item's description. Return the number of elements that were actually removed
	 * from the description.
	 */
	public int remove(Long id, Long[] arr) {
		//fiirst we convert the description into linked list and create a second list of items to be removed 
		int numOfWords = 0;
		if (itemMap.containsKey(id)) {
			Long[] desc = itemMap.get(id);
			LinkedList<Long> ll = new LinkedList<>(Arrays.asList(desc));
			LinkedList<Long> wordsToRemove = new LinkedList<>();
			for (Long l : ll) {
				for (Long word : arr) {
					if (l == word) {
						wordsToRemove.add(l);
						numOfWords++;
					}
				}
			}
			//the word and occurrence item map is updated 
			for (Long word : wordsToRemove) {
				if (itemCountMap.containsKey(word)) {
					HashSet<Long> items = itemCountMap.get(word);
					items.remove(id);
					itemCountMap.put(word, items);
				}
			}
			// the item map updated
			for (Long removeWord : wordsToRemove) {
				ll.remove(removeWord);
			}
			desc = ll.toArray(new Long[ll.size()]);
			itemMap.put(id, desc);
		}
		return numOfWords;
	}

	/*
	 * remove the elements of the array from the description of all items. Return
	 * the number of items that lost one or more terms from their descriptions.
	 */
	public int removeAll(Long[] arr) {
		//similar logic to remove operation but for all the elements.
		HashSet<Long> itemsToRemove = new HashSet<>();
		for(Long word : arr) {
			
			if(itemCountMap.containsKey(word)) {
				HashSet<Long> itemsSet = itemCountMap.remove(word);
				itemsToRemove.addAll(itemsSet);
				for(Long itemId :itemsSet) {
					Long descriptionArr[] = itemMap.get(itemId);
					LinkedList<Long> ll = new LinkedList<>(Arrays.asList(descriptionArr));
					Iterator<Long> it = ll.iterator();
					while(it.hasNext()) {
						Long element = it.next();
						if(element==word) {
							it.remove();
						}
						
					}
					descriptionArr = ll.toArray(new Long[ll.size()]);
					itemMap.put(itemId, descriptionArr);
				}
			}
			
		}
		return itemsToRemove.size();
	}
}
