package com.ptms.service; import org.springframework.stereotype.Service; import java.util.*;
@Service public class TicketCombinationService{
 private static final List<Integer>A=List.of(12,8,6,4,2,1);
 public List<List<Integer>> combinationsFor(int h){if(h<=0)return List.of(List.of());List<List<Integer>>r=new ArrayList<>();search(h,0,new ArrayList<>(),r);int m=r.stream().mapToInt(List::size).min().orElse(0);return r.stream().filter(x->x.size()==m).distinct().limit(5).toList();}
 private void search(int rem,int start,List<Integer>cur,List<List<Integer>>out){if(rem==0){out.add(List.copyOf(cur));return;}for(int i=start;i<A.size();i++){int v=A.get(i);if(v>rem)continue;cur.add(v);search(rem-v,i,cur,out);cur.remove(cur.size()-1);}}
}
