package softuni.exam.models.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class ApartmentIdDTO {
    @XmlElement
    private long id;

    public ApartmentIdDTO() {
    }

    public long getId() {
        return id;
    }
}
