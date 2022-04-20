package softuni.exam.models.dto;

import javax.validation.constraints.Positive;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

@XmlRootElement(name = "offer")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImportOfferDTO {
    @XmlElement
    @Positive
    private BigDecimal price;

    @XmlElement(name = "agent")
    private NameDTO agent;

    @XmlElement(name = "apartment")
    private ApartmentIdDTO apartment;

    @XmlElement
    private String publishedOn;

    public ImportOfferDTO() {
    }

    public BigDecimal getPrice() {
        return price;
    }

    public NameDTO getAgent() {
        return agent;
    }

    public ApartmentIdDTO getApartment() {
        return apartment;
    }

    public String getPublishedOn() {
        return publishedOn;
    }
}
