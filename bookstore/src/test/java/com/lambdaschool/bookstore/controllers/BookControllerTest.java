package com.lambdaschool.bookstore.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambdaschool.bookstore.BookstoreApplication;
import com.lambdaschool.bookstore.models.Author;
import com.lambdaschool.bookstore.models.Book;
import com.lambdaschool.bookstore.models.Section;
import com.lambdaschool.bookstore.models.Wrote;
import com.lambdaschool.bookstore.services.BookService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.config.http.MatcherType.mvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.function.RequestPredicates.accept;


@RunWith(SpringRunner.class)

/*****
 * Due to security being in place, we have to switch out WebMvcTest for SpringBootTest
 * @WebMvcTest(value = BookController.class)
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BookstoreApplication.class,
    properties = {
    "command.line.runner.enabled=false"
    })

/****
 * This is the user and roles we will use to test!
 */

@WithMockUser(username = "admin",
    roles = {"ADMIN", "DATA"})
public class BookControllerTest
{
    /******
     * WebApplicationContext is needed due to security being in place.
     */

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    List<Book> bookList = new ArrayList<>();

    @Before
    public void setUp() throws
            Exception
    {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
        /*****
         * The following is needed due to security being in place!
         */

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();

        /*****
         * Note that since we are only testing bookstore data, you only need to mock up bookstore data.
         * You do NOT need to mock up user data. You can. It is not wrong, just extra work.
         */
        Author a1 = new Author("Ian", "Stewart");
        a1.setAuthorid(20L);

        Section s1 = new Section("Fiction");
        s1.setSectionid(21L);

        Book b1 = new Book("Flatterland", "9780738206752", 2001, s1);
        b1.getWrotes()
            .add(new Wrote(a1, new Book()));

       bookList.add(b1);


    }

    @After
    public void tearDown() throws
            Exception
    {

    }

    @Test
    public void listAllBooks() throws
            Exception
    {
        String apiUrl = "/books/books";

        Mockito.when(bookService.findAll())
            .thenReturn(bookList);
        RequestBuilder rb = MockMvcRequestBuilders.get(apiUrl)
            .accept(MediaType.APPLICATION_JSON);

        MvcResult r = mockMvc.perform(rb)
        .andReturn();

        String tr = r.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        String er = mapper.writeValueAsString(bookList);

        System.out.println("Expect: " + er);
        System.out.println("Actual" + tr);

        assertEquals("Rest API Returns List",
            er,
            tr);

    }

    @Test
    public void getBookById() throws
            Exception
    {
        String apiUrl = "/books/book/1";
        Mockito.when(bookService.findBookById(1))
            .thenReturn(bookList.get(0));

        RequestBuilder rb = MockMvcRequestBuilders.get(apiUrl)
            .accept(MediaType.APPLICATION_JSON);

        MvcResult r = mockMvc.perform(rb)
            .andReturn();
        String tr = r.getResponse()
            .getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        String er = mapper.writeValueAsString(bookList.get(0));

        System.out.println("Expect: " + er);
        System.out.println("Actual: " + tr);

        assertEquals("Rest API Returns Book", er, tr);
    }

    @Test
    public void getNoBookById() throws
            Exception
    {
        String apiUrl = "/books/book/123232";

        Mockito.when(bookService.findBookById(123232))
            .thenReturn(null);

        RequestBuilder rb = MockMvcRequestBuilders.get(apiUrl)
            .accept(MediaType.APPLICATION_JSON);

        MvcResult r = mockMvc.perform(rb)
            .andReturn();
        String tr = r.getResponse()
            .getContentAsString();


        String er = "";

        System.out.println("Expect: " + er);
        System.out.println("Actual: " + tr);

        assertEquals("Rest API Returns Book Empty", er, tr);
    }

    @Test
    public void addNewBook() throws
            Exception
    {
        String apiUrl = "/books/book";
        Mockito.when(bookService.save(any(Book.class)))
            .thenReturn(bookList.get(0));

        String requestBody = "{ " +
            "    \"title\": \"ansdewuser\", " +
            "    \"isbn\": 80000.0, " +
            "    \"copy\": 2004 " +
            " }";


        RequestBuilder rb = MockMvcRequestBuilders.post(apiUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestBody);

        mockMvc.perform(rb)
            .andExpect(status().isCreated())
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void updateFullBook() throws Exception
    {
        String apiUrl = "/books/book/29";

        Section s1 = new Section();
        s1.setSectionid(22);
        s1.setName("Unknown");

        Author a1 = new Author();
        a1.setAuthorid(18);

        Book b1 = new Book("Some new book", "9999999", 2015, s1);
        b1.getWrotes().add(new Wrote(a1, new Book()));
        ObjectMapper mapper = new ObjectMapper();
        String bookString = mapper.writeValueAsString(b1);

        Mockito.when(bookService.save(any(Book.class)))
            .thenReturn(b1);


/**
        String requestBody = "{ " +
            "     \"bookid\": 29, " +
            "    \"title\": \"ansdesdwuser\", " +
            "    \"isbn\": \"80000540\", " +
            "    \"copy\": 2004, " +
            "    \"section\": { " +
            "      \"name\": \"Fiction\", " +
            "     }, " +
            "    \"wrotes\": [ " +
            "            { " +
            "        \"author\": { " +
            "        \"fname\": \"Ians\", " +
            "        \"lname\": \"Stewarts\", " +
            "     } " +
            "  } " +
            "  ] " +
            " }";
**/

        RequestBuilder rb = MockMvcRequestBuilders.put(apiUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(bookString);

        mockMvc.perform(rb)
            .andExpect(status().isOk())
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void deleteBookById() throws
            Exception
    {
        String apiUrl = "/books/book/29";

        RequestBuilder rb  = MockMvcRequestBuilders.delete(apiUrl, "29")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(rb)
            .andExpect(status().is2xxSuccessful())
            .andDo(MockMvcResultHandlers.print());
    }
}
