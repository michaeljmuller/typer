package org.themullers.typer;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcDao {

    private JdbcTemplate template;
    
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }
    
    public void updatePosition(int pos) {
        template.update("insert into progress (text_id, line) values (?, ?) ON CONFLICT (text_id) DO UPDATE set line = excluded.line", 1, pos);
    }
}
