package core;

import java.util.ArrayList;
import java.util.List;

public class ParsedQuery {
    private List<String> contentTerms;
    private List<String> pathTerms;
    private List<String> freeTerms;
    private SortStrategy sortStrategy;

    public enum SortStrategy { SCORE, NAME, DATE_MODIFIED }

    public ParsedQuery(){
        contentTerms = new ArrayList<String>();
        pathTerms = new ArrayList<String>();
        freeTerms = new ArrayList<String>();
        sortStrategy = SortStrategy.SCORE;
    }

    public void addContentTerm(String term){
        contentTerms.add(term);
    }
    public void addPathTerm(String term){
        pathTerms.add(term);
    }
    public void addFreeTerm(String term){
        freeTerms.add(term);
    }
    public void setSortStrategy(SortStrategy sortStrategy){
        this.sortStrategy = sortStrategy;
    }
    public List<String> getContentTerms(){
        return contentTerms;
    }
    public List<String> getPathTerms(){
        return pathTerms;
    }
    public List<String> getFreeTerms(){
        return freeTerms;
    }
    public SortStrategy getSortStrategy(){
        return sortStrategy;
    }

}
