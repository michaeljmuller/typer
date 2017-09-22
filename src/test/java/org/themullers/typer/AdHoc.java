package org.themullers.typer;

import java.io.File;

import org.junit.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.themullers.typer.textsource.Book;

public class AdHoc {
    
    @Test
    public void test() throws Exception {
        Book book = new Book(findTestFile("p-g-wodehouse_right-ho-jeeves.epub"));
        TextPreparationService prep = new TextPreparationService();
        for (String line : prep.toLines(book.toString())) {
            System.out.println(line);
        }
    }
    
    public void dbTest() throws Exception {
        JdbcDao db = new JdbcDao();
        DataSourceBuilder builder = DataSourceBuilder.create();
        builder.driverClassName("org.postgresql.Driver");
        builder.username("typer");
        builder.password("typer");
        builder.url("jdbc:postgresql://localhost:5432/typer");
        db.setDataSource(builder.build());
        db.updatePosition(200);
    }
    
    protected File findTestFile(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile());
    }
    
}
