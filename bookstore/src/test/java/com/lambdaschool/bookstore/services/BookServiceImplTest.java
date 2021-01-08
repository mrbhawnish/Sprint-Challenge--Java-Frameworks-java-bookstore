package com.lambdaschool.bookstore.services;

import com.lambdaschool.bookstore.BookstoreApplication;
import com.lambdaschool.bookstore.exceptions.ResourceNotFoundException;
import com.lambdaschool.bookstore.models.Author;
import com.lambdaschool.bookstore.models.Book;
import com.lambdaschool.bookstore.models.Section;
import com.lambdaschool.bookstore.models.Wrote;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BookstoreApplication.class)
//**********
// Note security is handled at the controller, hence we do not need to worry about security here!
//**********
public class BookServiceImplTest
{

    @Autowired
    private BookService bookService;

    @Before
    public void setUp() throws
            Exception
    {

        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws
            Exception
    {
    }

    @Test
    public void findAll()
    {
        assertEquals(4, bookService.findAll().size());
    }

    @Test
    public void findBookById()
    {
        assertEquals("Flatterland",
            bookService.findBookById(26).getTitle());
    }


    @Test(expected = ResourceNotFoundException.class)
    public void notFindBookById()
    {
        assertEquals("Flatterland",
            bookService.findBookById(20L).getTitle());
    }

    @Test
    public void delete()
    {
        bookService.delete(26);
        assertEquals(4, bookService.findAll()
            .size());
    }

    @Test
    public void save()
    {
        Author a1 = new Author("Ian", "Stewart");
        a1.setAuthorid(20L);

        Section s1 = new Section("Fiction");
        s1.setSectionid(21L);

        Book b1 = new Book("Flatterland", "9780738206752", 2001, s1);
        b1.getWrotes()
            .add(new Wrote(a1, new Book()));

        Book addBook = bookService.save(b1);

        assertNotNull(addBook);

        assertEquals(b1.getTitle(), addBook.getTitle());
    }

    @Test
    public void deleteAll()
    {
        bookService.deleteAll();
        assertEquals(0, bookService.findAll()
            .size());
    }
}