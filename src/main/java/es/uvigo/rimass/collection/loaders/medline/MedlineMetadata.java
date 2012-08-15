package es.uvigo.rimass.collection.loaders.medline;

import es.uvigo.rimass.collection.loaders.medline.jaxb.*;
import es.uvigo.rimass.core.Metadata;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.*;

/**
 *
 * @author ribadas
 */
@XmlRootElement
@XmlType(propOrder = {"authors", "affiliation", "journal", "date", "keyWords", "meshDescriptors"})
public class MedlineMetadata extends Metadata {

    private String PMID;
    private List<String> authors;
    private String affiliation;
    private MedlineJournal journal;
    private Date date;
    private List<String> keyWords;
    private List<String> meshDescriptors;

    public MedlineMetadata() {
        super("medline");
    }

    public MedlineMetadata(MedlineCitation medlineCitation) {
        super("medline");
        if (medlineCitation.getArticle() != null) {
            this.PMID = medlineCitation.getPMID().getvalue();

            Article article = medlineCitation.getArticle();

            this.date = extractDate(article);
            this.authors = extractAuthors(article);
            this.affiliation = extractAffilation(article);
            this.journal = extractJournal(article);
            this.keyWords = extractKeyWords(medlineCitation);
            this.meshDescriptors = extractMeshDescriptors(medlineCitation);
        }
    }

    @XmlAttribute
    public String getPMID() {
        return PMID;
    }

    public void setPMID(String PMID) {
        this.PMID = PMID;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    @XmlElementWrapper
    @XmlElement(name = "author")
    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public MedlineJournal getJournal() {
        return journal;
    }

    public void setJournal(MedlineJournal journal) {
        this.journal = journal;
    }

    @XmlElementWrapper
    @XmlElement(name = "keyword")
    public List<String> getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(List<String> keyWords) {
        this.keyWords = keyWords;
    }

    @XmlElementWrapper
    @XmlElement(name = "meshdescriptor")
    public List<String> getMeshDescriptors() {
        return meshDescriptors;
    }

    public void setMeshDescriptors(List<String> meshDescriptors) {
        this.meshDescriptors = meshDescriptors;
    }

    private Date extractDate(Article article) {
        if ((article.getArticleDate() != null) && (!article.getArticleDate().isEmpty())) {
            ArticleDate articleDate = article.getArticleDate().get(0);
            int year = Integer.valueOf(articleDate.getYear().getvalue());
            int month = Integer.valueOf(articleDate.getMonth().getvalue());
            int day = Integer.valueOf(articleDate.getDay().getvalue());
            Calendar aux = Calendar.getInstance();
            aux.set(year, month, day);
            return aux.getTime();
        } else {
            return null;
        }
    }

    private List<String> extractAuthors(Article article) {
        List<String> result = new ArrayList<String>();
        if ((article.getAuthorList() != null) && (article.getAuthorList().getAuthor() != null)) {
            for (Author author : article.getAuthorList().getAuthor()) {
                if (author.getLastNameOrForeNameOrInitialsOrSuffixOrCollectiveName() != null) {
                    StringBuilder name = new StringBuilder();
                    for (Object part : author.getLastNameOrForeNameOrInitialsOrSuffixOrCollectiveName()) {
                        if (name.length() > 0) {
                            name.append(' ');
                        }
                        if (part instanceof LastName) {
                            name.append(((LastName) part).getvalue());
                        } else if (part instanceof ForeName) {
                            name.append(((ForeName) part).getvalue());
                        } else if (part instanceof Initials) {
                            name.append(((Initials) part).getvalue());
                        } else if (part instanceof Suffix) {
                            name.append(((Suffix) part).getvalue());
                        } else if (part instanceof CollectiveName) {
                            name.append(((CollectiveName) part).getvalue());
                        }

                    }
                    result.add(name.toString());
                }
            }
        }
        return result;
    }

    private MedlineJournal extractJournal(Article article) {
        if (article.getJournal() != null) {
            String name = article.getJournal().getTitle();
            String issn = article.getJournal().getISSN().getvalue();
            String issue = article.getJournal().getJournalIssue().getIssue();
            String volume = article.getJournal().getJournalIssue().getVolume();
            String date="";
            if (article.getJournal().getJournalIssue().getPubDate() != null) {
                            StringBuilder builder = new StringBuilder();

                for (Object part : article.getJournal().getJournalIssue().getPubDate().getYearOrMonthOrDayOrSeasonOrMedlineDate()) {
                    if (builder.length() != 0) {
                        builder.append(' ');
                    }
                    if (part instanceof Year) {
                        builder.append(((Year) part).getvalue());
                    } else if (part instanceof Month) {
                        builder.append(((Month) part).getvalue());
                    } else if (part instanceof Day) {
                        builder.append(((Day) part).getvalue());
                    } else if (part instanceof Season) {
                        builder.append(((Season) part).getvalue());
                    } else if (part instanceof MedlineDate) {
                        builder.append(((MedlineDate) part).getvalue());
                    }                    
                }
                date = builder.toString();
            }
            String pages = "";
            if (article.getPaginationOrELocationID() != null) {
                if (article.getPaginationOrELocationID() instanceof Pagination) {
                    StringBuilder builder = new StringBuilder();
                    for (Object part : ((Pagination) article.getPaginationOrELocationID()).getStartPageOrEndPageOrMedlinePgn()) {
                        if (builder.length() > 0) {
                            builder.append(' ');
                        }
                        if (part instanceof StartPage) {
                            builder.append(((StartPage) part).getvalue());
                        } else if (part instanceof EndPage) {
                            builder.append(((EndPage) part).getvalue());
                        } else if (part instanceof MedlinePgn) {
                            builder.append(((MedlinePgn) part).getvalue());
                        }
                    }
                    pages = builder.toString();
                } else if (article.getPaginationOrELocationID() instanceof ELocationID) {
                    pages = ((ELocationID) article.getPaginationOrELocationID()).getvalue();
                }
            }
            return new MedlineJournal(name, issn, issue, volume, pages, date);
        } else {
            return null;
        }
    }

    private List<String> extractKeyWords(MedlineCitation medlineCitation) {
        List<String> result = new ArrayList<String>();
        if (medlineCitation.getKeywordList() != null) {
            for (KeywordList kwl : medlineCitation.getKeywordList()) {
                if (kwl.getKeyword() != null) {
                    for (Keyword kw : kwl.getKeyword()) {
                        result.add(kw.getvalue());
                    }
                }
            }
        }

        return result;
    }

    private List<String> extractMeshDescriptors(MedlineCitation medlineCitation) {
        List<String> result = new ArrayList<String>();
        if ((medlineCitation.getMeshHeadingList() != null)
                && (medlineCitation.getMeshHeadingList().getMeshHeading() != null)) {
            for (MeshHeading mh : medlineCitation.getMeshHeadingList().getMeshHeading()) {
                if (mh.getDescriptorName() != null) {
                    result.add(mh.getDescriptorName().getvalue());
                }
            }
        }

        return result;
    }

    private String extractAffilation(Article article) {
        return article.getAffiliation();
    }
}
