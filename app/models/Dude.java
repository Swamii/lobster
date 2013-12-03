package models;

import javax.persistence.*;

@Entity
@NamedQueries({
    @NamedQuery(name="Dude.findAll",
                query="SELECT d FROM Dude d")
})
public class Dude {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Column
    public String name;

}
