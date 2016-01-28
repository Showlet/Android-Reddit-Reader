package com.example.reddit;

/**
 * Created by vincent on 10/23/15.
 */
public abstract class FetchCompleted implements Runnable
{
    protected String m_result;

    public void setResult(String result)
    {
        m_result = result;
    }
}
