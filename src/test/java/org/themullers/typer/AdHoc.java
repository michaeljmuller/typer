package org.themullers.typer;

import java.io.File;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.themullers.typer.textsource.Book;

public class AdHoc {
    
    public void test() throws Exception {
        Book book = new Book(findTestFile("p-g-wodehouse_right-ho-jeeves.epub"));
        TextPreparationService prep = new TextPreparationService();
        for (String line : prep.toLines(book.toString())) {
            System.out.println(line);
        }
    }
    
    @Test
    public void dbTest() throws Exception {
        JdbcDao db = new JdbcDao();
        db.setDataSource(buildDataSource());
        db.updatePosition(200);
        assert(db.getPosition() == 200);
    }
    
    
    protected DataSource buildDataSource() {
        DataSourceBuilder builder = DataSourceBuilder.create();
        builder.driverClassName("org.postgresql.Driver");
        builder.username("typer");
        builder.password("typer");
        builder.url("jdbc:postgresql://localhost:5432/typer");
        return builder.build();
    }
    
    protected File findTestFile(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile());
    }
    
}
