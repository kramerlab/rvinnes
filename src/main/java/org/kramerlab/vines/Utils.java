package org.kramerlab.vines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.kramerlab.copulae.*;

/**
 * This class contains utility function.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class Utils{
	private static boolean debug = false;
	
	/**
	 * Get the maximum spanning tree.
	 * <br>
	 * This function is specialized for Regular Vines.
	 * For that purpose, it uses absolute Edge weights to
	 * calculate the maximum spanning tree.
	 * <br>
	 * The result is a spanning tree, which represents the
	 * most correlating variable pairs for the RVine.
	 * <br>
	 * It uses the idea of Prim's MST algorithm,
	 * but searching for the maximum spanning tree instead
	 * of the minimum spanning tree.
	 * @param g a connected Graph.
	 * @return returns the maximum spanning tree of g.
	 */
	public static Graph maxSpanTree(Graph g){
		if(g == null){
			return null;
		}
		if(g.isEmpty()){
			return null;
		}
		
		Graph maxST = new Graph();
		
		ArrayList<Node> nodeList = g.getNodeList();
		for(Node n: nodeList){
			maxST.addNode(n);
		}
		
		ArrayList<Node> nodeListMaxST = new ArrayList<Node>();
		nodeListMaxST.add(nodeList.get((int) (Math.random()*nodeList.size())));
		
		while(!(nodeListMaxST.size() == nodeList.size())){
			ArrayList<Edge> edges = new ArrayList<Edge>();
			for(Node n : nodeListMaxST){
				edges.addAll(g.getGraph().get(n));
			}
			
			Edge maxEdge = null;
			for(Edge e : edges){
				if(! nodeListMaxST.contains(e.getTo()) ){
					if(maxEdge == null){
						maxEdge = e;
					}else{
						if(Math.abs(maxEdge.getWeight())
								< Math.abs(e.getWeight())){
							maxEdge = e;
						}
					}
				}
			}

			nodeListMaxST.add(maxEdge.getTo());
			maxST.addEdge(maxEdge);
		}
		return maxST;
	}
	
	/**
	 * Calculates the empirical Kendall's tau.
	 * <br>
	 * Both random variables need to be rank normalized to get a 
	 * reliable Kendall's tau value.
	 * @param a a rank normalized random variable.
	 * @param b another rank normalized random variable.
	 * @return returns the empirical Kendall's tau for a and b.
	 */
	public static double kendallsTau(double[] a, double[] b){
		if(!( a.length == b.length)){
			return Double.NaN;
		}
		
		//Sort lists with respect to a
		ArrayList<Double> x = new ArrayList<Double>();
		ArrayList<Double> y = new ArrayList<Double>();
		
		for(int i=0;i<a.length;i++){
			double in = a[i];
			int k;
			for(k=0;k<x.size();k++){
				if(in < x.get(k)){
					break;
				}
			}
			x.add(k, in);
			y.add(k, b[i]);
		}
		if (debug){
			System.out.println("a = "+a);
			System.out.println("b = "+b);
			System.out.println("x = "+x);
			System.out.println("y = "+y);
		}
		//Begin counting:
		
		int P = 0; // number of concordant pairs
		int Q = 0; // number of discordant pairs
		int T = 0; // number of ties only in a
		int U = 0; // number of ties only in b
		//if a tie appears in a and b it is not added to either T or U
		
		if (debug){
			System.out.println();
		}
		for(int i=0;i<x.size()-1;i++){
			for(int j=i+1;j<y.size();j++){
				if (debug){
					System.out.println("Comparing "+i+" to "+j);
				}
				if(x.get(i) < x.get(j) && y.get(i) < y.get(j)){
					P++;
				}
				if(x.get(i) < x.get(j) && y.get(i) > y.get(j)){
					Q++;
				}
				if(x.get(i).equals(x.get(j)) && !(y.get(i).equals(y.get(j)))){
					T++;
				}
				if(!(x.get(i).equals(x.get(j))) && y.get(i).equals(y.get(j))){
					U++;
				}
				if (debug){
					System.out.println("P: "+P +", Q: "+Q+", T: "+T+", U:"+U);
				}
			}
		}
		if (debug){
			System.out.println();
			System.out.println("P: "+P +", Q: "+Q+", T: "+T+", U:"+U);
		}
		double n = (P+Q+T);
		double m = (P+Q+U);
		return (P-Q) / Math.sqrt(n*m);
	}
	
	public static Copula[] copulae(boolean[] c){
		int k=0;
		/*
		 * 0 - Independence Copula
		 * 1 - Gauss Copula
		 * 2 - T Copula
		 * 3 - Clayton Copula
		 * 4 - Frank Copula
		 * 5 - Gumbel Copula
		 * 6 - FGM Copula
		 * 7 - Galambos Copula
		 */
		
		for(int i=0; i<c.length; i++){
			if(c[i]){
				// rotations for Clayton (3) and Gumbel (5)
				if(i==3 || i==5) k+=3;
				k++;
			}
		}
		
		Copula[] out = new Copula[k];
		
		k=0;
		for(int i=0; i<c.length; i++){
			if(c[i]){
				if(i==0) out[k] = new IndependenceCopula();
				if(i==1) out[k] = new GaussCopula(new double[]{0.5});
				if(i==2) out[k] = new TCopula(new double[]{0.5, 1});
				if(i==3){
					out[k] = new ClaytonCopula(new double[]{2});
					Clayton90RotatedCopula c1 = new Clayton90RotatedCopula(new double[]{-2});
					out[k+1] = c1;
					Clayton180RotatedCopula c2 = new Clayton180RotatedCopula(new double[]{2});
					out[k+2] = c2;
					Clayton270RotatedCopula c3 = new Clayton270RotatedCopula(new double[]{-2});
					out[k+3] = c3;
					k+=3;
				}
				if(i==4) out[k] = new FrankCopula(new double[]{0.5});
				if(i==5){
					out[k] = new GumbelCopula(new double[]{3});
					Gumbel90RotatedCopula c1 = new Gumbel90RotatedCopula(new double[]{-3});
					out[k+1] = c1;
					Gumbel180RotatedCopula c2 = new Gumbel180RotatedCopula(new double[]{3});
					out[k+2] = c2;
					Gumbel270RotatedCopula c3 = new Gumbel270RotatedCopula(new double[]{-3});
					out[k+3] = c3;
					k+=3;
				}
				if(i==6) out[k] = new FGMCopula(new double[]{0});
				if(i==7) out[k] = new GalambosCopula(new double[]{0});
				k++;
			}
		}
		
		return out;
	}
	
	public static Copula fitCopula(Copula[] copulae,
			double[] a, double[] b){
		
		double[] lls = new double[copulae.length];
		
		for(int i=0; i<copulae.length; i++){
			Copula c = copulae[i];
			lls[i] = c.mle(a, b);
			// System.out.println(c.name()+" with par="+c.getParams()[0]+" produces "+lls[i]+" log-likelihood.");
		}
		// System.out.println();
		
		int out = 0;
		for(int i=1; i<copulae.length; i++){
			if(lls[out] < lls[i]) out = i;
		}
		
		return copulae[out];
	}
	
	/**
	 * This is a placeholder for a goodness of fit test.
	 * <br>
	 * It test the copulae for fitting between a and b.
	 * The best copula will be returned with its parameters.
	 * <br>
	 * Because the RVine is currently using only Gauss copulae,
	 * this method returns the MLE on the Gauss copula function.
	 * 
	 * @param copulae an array of copula families,
	 * that shall participate on the GOF-Test.
	 * @param a an observation array.
	 * @param b another observation array.
	 * @return returns the copula with its parameters that fits best.
	 */
	public static Copula goodnessOfFit(Copula[] copulae,
			double[] a, double[] b){
		
		double[] p = new double[copulae.length];
		
		for(int i=0; i<copulae.length; i++){
			Copula c = copulae[i];
			p[i] = pValue(c, a, b);
		}
		
		int out = 0;
		for(int i=1; i<copulae.length; i++){
			if(p[out] < p[i]) out = i;
		}
		
		return copulae[out];
	}
	
	//Bootstrap method for p-value computation
	private static double pValue(Copula c, double[] a, double[] b){
		int N = 100;
		int n = a.length;
		
		c.mle(a, b);
		
		double sn = 0;
		for(int i=0; i<n; i++){
			sn += Math.pow(empCop(a, b, a[i], b[i]) - c.C(a[i], b[i]), 2);
		}
		
		int hitCount = 0;
		
		for(int k=0; k<N; k++){
			// generate random samples
			double[] a2 = new double[n];
			double[] b2 = new double[n];
			
			for(int i=0; i<n; i++){
				a2[i] = Math.random();
				b2[i] = c.h2inverse(a2[i], Math.random());
			}
			
			double[] u1 = rankNormalization(a2);
			double[] u2 = rankNormalization(b2);
			
			c.mle(u1, u2);
			
			double sn2 = 0;
			for(int i=0; i<n; i++){
				sn2 += Math.pow(empCop(u1, u2, u1[i], u2[i]) - c.C(u1[i], u2[i]), 2);
			}
			
			if(sn2 > sn) hitCount++;
			
		}
		
		return hitCount/((double) N);
	}
	
	private static double empCop(double[] a, double[] b, double x, double y){
		int N = a.length;
		int obs = 0;
		
		for(int i=0; i<N; i++){
			if(a[i] <= x && b[i] <= y) obs++;
		}
		
		return 1.0/N*obs;
	}
	
	
	/**
	 * Log-Likelihood calculation for copulae.
	 * <br>
	 * It is used to calculate the copula log-likelihood for the MLE.
	 * 
	 * @param c a copula, whose log-likelihood is calculated.
	 * @param a an observation array.
	 * @param b another observation array.
	 * @return returns the log-likelihood.
	 */
	public static double logLikelihood(Copula c, double[] a, double[] b){
		double logLik = 0;
		
		for(int i=0;i<a.length;i++){
			logLik += Math.log(c.density(a[i], b[i]));
		}
		return logLik;
	}
	
	/**
	 * Get rank normalized data.
	 * @param data the data to be rank normalized.
	 * @return the rank normalized data.
	 */
	public static double[] rankNormalization(double[] data){
		
		// Get a copy of the list, with values sorted from least to greatest.
		// That is: S[i] <= S[2] <= ... <= S[N]
		// Thus, the index of each list item equates to its ranking position
		
		ArrayList<Double> S = new ArrayList<Double>();
		
		for(double e : data){
			S.add(e);
		}
		
		Collections.sort(S);
		
		// Create a lookup table from value to ranking position
		// Correct for ties in the sorted list by average as we go.
		
		double tieCount = 0;
		double tieTotal = 0;
		double tieValue = Double.NaN;
		HashMap<Double, Double> table = new HashMap<Double, Double>();
		
		for(int i=1; i<=S.size(); i++){
			if(S.get(i-1) == tieValue){
				tieTotal += i;
				tieCount++;
			}else{
				table.put(S.get(i-1), (double) i);
				
				// If we are coming off a run of ties, find the avg
				// and use that as the ranking position for that value
				
				if(tieCount > 1){
					table.put(tieValue, tieTotal/tieCount);
				}
				
				tieCount = 1;
				tieTotal = i;
				tieValue = S.get(i-1);
			}
		}
		
		// Check if S ended in a run of ties and treat as above
		
		if(tieCount > 1){
			table.put(tieValue, tieTotal/tieCount);
		}
		
		// Now, create a new list of ranking positions corresponding
		// to the order of the original list of values.
		
		double[] out = new double[data.length];
		double maxRank = tieTotal/tieCount;
		
		for(int i=0; i<data.length; i++){
			// normalize with dividing by max rank
			out[i] = table.get(data[i])/maxRank;
		}
		
		return out;
	}
	
	/**
	 * Creates the conditioned set from two Nodes.
	 * <br>
	 * See the constraint set definitions presented
	 * in J.F. Di&szlig;mann's diploma thesis.
	 * @param a a Node.
	 * @param b another Node.
	 * @return conditioned set created by both nodes.
	 */
	public static ArrayList<Integer> createConditionedSet(Node a, Node b){
		TreeSet<Integer> U_a = new TreeSet<Integer>(a.set());
		TreeSet<Integer> U_b = new TreeSet<Integer>(b.set());
		TreeSet<Integer> D = new TreeSet<Integer>(U_a);
		D.retainAll(U_b);
		
		TreeSet<Integer> C = new TreeSet<Integer>(U_a);
		C.addAll(U_b);
		C.removeAll(D);
		ArrayList<Integer> Ca = new ArrayList<Integer>(C);
		Collections.sort(Ca);
		return Ca;
	}
	
	/**
	 * Correction function.
	 * <br>
	 * Corrects the value of x.
	 * <br>
	 * It is used for infinity value handling.
	 * @param x the value to be corrected.
	 * @return returns the correction of x.
	 */
	public static double laplaceCorrection(double x){
		x = Math.min(x,1-Math.pow(10, -4));
		x = Math.max(x,Math.pow(10, -4));
		return x;
	}
	
	public static double simpsonIntegrate(UnivariateFunction f, int N, double lb, double ub){
	      double h = (ub - lb) / N;     // step size
	      
	      double sum = f.value(lb) + f.value(ub);
	      
	      for (int i = 1; i < N; i++) {
	         double x = lb + h * i;
	         sum += 2 * f.value(x);
	      }
	      
	      for (int i = 1; i <= N; i++) {
	         double x = lb + h * (i-1);
	         double y = lb + h * i;
	         sum += 4 * f.value((x+y)/2);
	      }
	      
	      return sum * h / 6.0;
	}
	
	//https://github.com/tnagler/VineCopula/blob/master/src/hfunc.c
	public static double bisectionInvert(UnivariateFunction f, double z, double lb, double ub){
		boolean br = false;
	    double ans = 0.0, tol = 0, x0 = lb, x1 = ub, it=0, fl, fh, val;
	    
	    fl = f.value(x0);
	    fl -= z;
	    fh = f.value(x1);
	    fh -= z;
	    
	    if (Math.abs(fl) <= tol) {
	        ans = x0;
	        br = true;
	    }
	    if (Math.abs(fh) <= tol) {
	        ans = x1;
	        br = true;
	    }

	    while (!br){
	        ans = (x0 + x1) / 2.0;
	        val = f.value(ans);
	        val -= z;

	        //stop if values become too close (avoid infinite loop)
	        if (Math.abs(val) <= tol) br = true;
	        if (Math.abs(x0-x1) <= tol) br = true;

	        if (val > 0.0) {
	            x1 = ans;
	            fh = val;
	        } else {
	            x0 = ans;
	            fl = val;
	        }

	        //stop if too many iterations are required (avoid infinite loop)
	        ++it;
	        if (it > 50) br = true;
	    }

	    return ans;
	}
}
