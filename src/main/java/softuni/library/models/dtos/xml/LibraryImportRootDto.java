package softuni.library.models.dtos.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "libraries")
@XmlAccessorType(XmlAccessType.FIELD)
public class LibraryImportRootDto {
    @XmlElement(name = "library")
    private List<LibraryImportDto> libraryImportDtos;

    public LibraryImportRootDto() {
    }

    public List<LibraryImportDto> getLibraryImportDtos() {
        return libraryImportDtos;
    }

    public void setLibraryImportDtos(List<LibraryImportDto> libraryImportDtos) {
        this.libraryImportDtos = libraryImportDtos;
    }
}
