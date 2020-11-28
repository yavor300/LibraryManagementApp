package softuni.library.services.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import softuni.library.models.dtos.xml.CharacterImportDto;
import softuni.library.models.dtos.xml.CharacterImportRootDto;
import softuni.library.models.dtos.xml.LibraryImportDto;
import softuni.library.models.dtos.xml.LibraryImportRootDto;
import softuni.library.models.entities.Book;
import softuni.library.models.entities.Character;
import softuni.library.models.entities.Library;
import softuni.library.repositories.BookRepository;
import softuni.library.repositories.LibraryRepository;
import softuni.library.services.LibraryService;
import softuni.library.util.ValidatorUtil;
import softuni.library.util.XmlParser;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class LibraryServiceImpl implements LibraryService {
    private static final String LIBRARIES_XML_PATH = "src/main/resources/files/xml/libraries.xml";
    private final LibraryRepository libraryRepository;
    private final BookRepository bookRepository;
    private final ValidatorUtil validatorUtil;
    private final XmlParser xmlParser;
    private final ModelMapper modelMapper;

    @Autowired
    public LibraryServiceImpl(LibraryRepository libraryRepository, BookRepository bookRepository, ValidatorUtil validatorUtil, XmlParser xmlParser, ModelMapper modelMapper) {
        this.libraryRepository = libraryRepository;
        this.bookRepository = bookRepository;
        this.validatorUtil = validatorUtil;
        this.xmlParser = xmlParser;
        this.modelMapper = modelMapper;
    }

    @Override
    public boolean areImported() {
        return this.libraryRepository.count() > 0;
    }

    @Override
    public String readLibrariesFileContent() throws IOException {
        return String.join("", Files.readAllLines(Path.of(LIBRARIES_XML_PATH)));
    }

    @Override
    public String importLibraries() throws JAXBException {
        StringBuilder sb = new StringBuilder();
        LibraryImportRootDto libraryImportRootDto = this.xmlParser.parseXml(LibraryImportRootDto.class, LIBRARIES_XML_PATH);

        for (LibraryImportDto dto : libraryImportRootDto.getLibraryImportDtos()) {
            Optional<Book> byId = this.bookRepository.findById(dto.getBook().getId());

            if (validatorUtil.isValid(dto) && byId.isPresent()) {

                if (this.libraryRepository.findByName(dto.getName()).isEmpty()) {
                    Library library = this.modelMapper.map(dto, Library.class);
                    this.libraryRepository.saveAndFlush(library);

                    sb.append(String.format("Successfully added Library: %s - %s",
                            dto.getName(), dto.getLocation()))
                            .append(System.lineSeparator());
                }

                Book book = byId.get();

                Library library = this.libraryRepository.findByName(dto.getName()).get();
                library.getBooks().add(book);

                this.libraryRepository.saveAndFlush(library);
            } else {
                sb.append("Invalid Library").append(System.lineSeparator());
            }
        }

        return sb.toString();
    }
}
