package org.infoglue.igide.cms;


public class Repository
{
    private Integer id;
    private String name;
    private String description;
    private String dnsName;

    public Repository()
    {
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDnsName()
    {
        return dnsName;
    }

    public void setDnsName(String dnsName)
    {
        this.dnsName = dnsName;
    }
}
