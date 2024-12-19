package Project.ProjectBackend.domain;

import Project.ProjectBackend.domain.item.Item;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Category {
    @Id
    @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name;
    @ManyToMany
    @JoinTable(name = "category_item",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name="parent_id")
    private Category parent;

    @OneToMany(mappedBy="parent") //같은 테이블(위 칼럼)에 조인
    private List<Category> child = new ArrayList<>();

}
