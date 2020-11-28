package softuni.library.services.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import softuni.library.models.dtos.xml.CharacterImportDto;
import softuni.library.models.dtos.xml.CharacterImportRootDto;
import softuni.library.models.entities.Book;
import softuni.library.models.entities.Character;
import softuni.library.repositories.BookRepository;
import softuni.library.repositories.CharacterRepository;
import softuni.library.services.CharacterService;
import softuni.library.util.ValidatorUtil;
import softuni.library.util.XmlParser;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class CharacterServiceImpl implements CharacterService {
    private static final String CHARACTERS_XML_PATH = "src/main/resources/files/xml/characters.xml";
    private final CharacterRepository characterRepository;
    private final BookRepository bookRepository;
    private final ValidatorUtil validatorUtil;
    private final XmlParser xmlParser;
    private final ModelMapper modelMapper;

    @Autowired
    public CharacterServiceImpl(CharacterRepository characterRepository, BookRepository bookRepository, ValidatorUtil validatorUtil, XmlParser xmlParser, ModelMapper modelMapper) {
        this.characterRepository = characterRepository;
        this.bookRepository = bookRepository;
        this.validatorUtil = validatorUtil;
        this.xmlParser = xmlParser;
        this.modelMapper = modelMapper;
    }

    @Override
    public boolean areImported() {
        return this.characterRepository.count() > 0;
    }

    @Override
    public String readCharactersFileContent() throws IOException {
        return String.join("", Files.readAllLines(Path.of(CHARACTERS_XML_PATH)));
    }

    @Override
    public String importCharacters() throws JAXBException {
        StringBuilder sb = new StringBuilder();
        CharacterImportRootDto characterImportRootDto = this.xmlParser.parseXml(CharacterImportRootDto.class, CHARACTERS_XML_PATH);

        for (CharacterImportDto dto : characterImportRootDto.getCharacterImportDtos()) {
            Optional<Book> byId = this.bookRepository.findById(dto.getBook().getId());
            if (validatorUtil.isValid(dto) && byId.isPresent()) {
                Character character = this.modelMapper.map(dto, Character.class);
                Book book = byId.get();

                character.setBook(book);

                this.characterRepository.saveAndFlush(character);

                sb.append(String.format("Successfully imported Character: %s %s - age: %d",
                        dto.getFirstName(), dto.getLastName(), dto.getAge()))
                        .append(System.lineSeparator());
            } else {
                sb.append("Invalid Character").append(System.lineSeparator());
            }
        }

        return sb.toString();
    }

    @Override
    public String findCharactersInBookOrderedByLastNameDescendingThenByAge() {
        StringBuilder sb = new StringBuilder();
        List<Character> characters = this.characterRepository.exportCharacters();
        for (Character character : characters) {
            sb.append(String.format("Character name %s %s %s, age %d, in book %s",
                    character.getFirstName(), character.getMiddleName(), character.getLastName(),
                    character.getAge(), character.getBook().getName()))
                    .append(System.lineSeparator());
        }

        return sb.toString().trim();
    }
}
