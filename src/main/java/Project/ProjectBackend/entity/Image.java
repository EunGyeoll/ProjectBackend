package Project.ProjectBackend.entity;

import Project.ProjectBackend.entity.Item;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @Column(nullable = false)
    private String originFileName;

    @Column
    private String newFileName;

    @Column
    private String imagePath; // 이미지 경로

    @Column
    private long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item; // 해당 이미지를 참조하는 Item


    public void setItem(Item item) {
        this.item = item;
        if (!item.getImages().contains(this)) {
            item.getImages().add(this);
        }
    }

}
