package softuni.exam.models.dto;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "apartment")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImportApartmentDTO {
    @XmlElement
    private String apartmentType;

    @XmlElement
    private double area;

    @XmlElement
    private String town;

    public ImportApartmentDTO() {
    }

    public String getApartmentType() {
        return apartmentType;
    }

    public double getArea() {
        return area;
    }

    public String getTown() {
        return town;
    }

    public boolean isValid() {
        if (!apartmentType.equals("two_rooms")
                && !apartmentType.equals("three_rooms")
                && !apartmentType.equals("four_rooms")) {
            return false;
        }

        if (area < 40.00) {
            return false;
        }

        return true;
    }
}
