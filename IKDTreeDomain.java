


import java.io.Serializable;

/**
 * 类IKDTreeDomain.java的实现描述：在kdtree 上查找的元素必须存在这两个字段
 * 
 *
 */
public abstract class IKDTreeDomain implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -6956627943184526276L;
    public int   dim;
    public int[] descriptor;
}

