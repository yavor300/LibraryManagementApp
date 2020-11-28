package softuni.library.services.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import softuni.library.models.dtos.jsons.BookImportDto;
import softuni.library.models.entities.Author;
import softuni.library.models.entities.Book;
import softuni.library.repositories.AuthorRepository;
import softuni.library.repositories.BookRepository;
import softuni.library.services.BookService;
import softuni.library.util.ValidatorUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {
    private static final String BOOKS_JSON_PATH = "src/main/resources/files/json/books.json";
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final Gson gson;
    private final ModelMapper modelMapper;
    private final ValidatorUtil validatorUtil;

    @Autowired
    public BookServiceImpl(BookRepository bookRepository, AuthorRepository authorRepository, Gson gson, ModelMapper modelMapper, ValidatorUtil validatorUtil) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.gson = gson;
        this.modelMapper = modelMapper;
        this.validatorUtil = validatorUtil;
    }

    @Override
    public boolean areImported() {
        return this.bookRepository.count() > 0;
    }

    @Override
    public String readBooksFileContent() throws IOException {
        return String.join("", Files.readAllLines(Path.of(BOOKS_JSON_PATH)));
    }

    @Override
    public String importBooks() throws IOException {
        StringBuilder sb = new StringBuilder();
        BookImportDto[] bookImportDtos = this.gson.fromJson(this.readBooksFileContent(), BookImportDto[].class);

        for (BookImportDto dto : bookImportDtos) {
            Optional<Author> byId = this.authorRepository.findById(dto.getAuthor());

            if (this.validatorUtil.isValid(dto) && byId.isPresent()) {
                Book book = this.modelMapper.map(dto, Book.class);
                book.setAuthor(byId.get());

                this.bookRepository.saveAndFlush(book);

                sb.append(String.format("Successfully imported Book: %s written in %s",
                        dto.getName(), dto.getWritten()))
                        .append(System.lineSeparator());
            } else {
                sb.append("Invalid Book").append(System.lineSeparator());
            }
        }

        return sb.toString();
    }
}
