/* This file was generated by SableCC (http://www.sablecc.org/). */

package soot.jimple.parser.node;

import soot.jimple.parser.analysis.*;

@SuppressWarnings("nls")
public final class ADynamicInvokeExpr extends PInvokeExpr
{
    private TDynamicinvoke _dynamicinvoke_;
    private TStringConstant _stringConstant_;
    private PUnnamedMethodSignature _dynmethod_;
    private TLParen _firstl_;
    private PArgList _dynargs_;
    private TRParen _firstr_;
    private PMethodSignature _bsm_;
    private TLParen _lParen_;
    private PArgList _staticargs_;
    private TRParen _rParen_;

    public ADynamicInvokeExpr()
    {
        // Constructor
    }

    public ADynamicInvokeExpr(
        @SuppressWarnings("hiding") TDynamicinvoke _dynamicinvoke_,
        @SuppressWarnings("hiding") TStringConstant _stringConstant_,
        @SuppressWarnings("hiding") PUnnamedMethodSignature _dynmethod_,
        @SuppressWarnings("hiding") TLParen _firstl_,
        @SuppressWarnings("hiding") PArgList _dynargs_,
        @SuppressWarnings("hiding") TRParen _firstr_,
        @SuppressWarnings("hiding") PMethodSignature _bsm_,
        @SuppressWarnings("hiding") TLParen _lParen_,
        @SuppressWarnings("hiding") PArgList _staticargs_,
        @SuppressWarnings("hiding") TRParen _rParen_)
    {
        // Constructor
        setDynamicinvoke(_dynamicinvoke_);

        setStringConstant(_stringConstant_);

        setDynmethod(_dynmethod_);

        setFirstl(_firstl_);

        setDynargs(_dynargs_);

        setFirstr(_firstr_);

        setBsm(_bsm_);

        setLParen(_lParen_);

        setStaticargs(_staticargs_);

        setRParen(_rParen_);

    }

    @Override
    public Object clone()
    {
        return new ADynamicInvokeExpr(
            cloneNode(this._dynamicinvoke_),
            cloneNode(this._stringConstant_),
            cloneNode(this._dynmethod_),
            cloneNode(this._firstl_),
            cloneNode(this._dynargs_),
            cloneNode(this._firstr_),
            cloneNode(this._bsm_),
            cloneNode(this._lParen_),
            cloneNode(this._staticargs_),
            cloneNode(this._rParen_));
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseADynamicInvokeExpr(this);
    }

    public TDynamicinvoke getDynamicinvoke()
    {
        return this._dynamicinvoke_;
    }

    public void setDynamicinvoke(TDynamicinvoke node)
    {
        if(this._dynamicinvoke_ != null)
        {
            this._dynamicinvoke_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._dynamicinvoke_ = node;
    }

    public TStringConstant getStringConstant()
    {
        return this._stringConstant_;
    }

    public void setStringConstant(TStringConstant node)
    {
        if(this._stringConstant_ != null)
        {
            this._stringConstant_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._stringConstant_ = node;
    }

    public PUnnamedMethodSignature getDynmethod()
    {
        return this._dynmethod_;
    }

    public void setDynmethod(PUnnamedMethodSignature node)
    {
        if(this._dynmethod_ != null)
        {
            this._dynmethod_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._dynmethod_ = node;
    }

    public TLParen getFirstl()
    {
        return this._firstl_;
    }

    public void setFirstl(TLParen node)
    {
        if(this._firstl_ != null)
        {
            this._firstl_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._firstl_ = node;
    }

    public PArgList getDynargs()
    {
        return this._dynargs_;
    }

    public void setDynargs(PArgList node)
    {
        if(this._dynargs_ != null)
        {
            this._dynargs_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._dynargs_ = node;
    }

    public TRParen getFirstr()
    {
        return this._firstr_;
    }

    public void setFirstr(TRParen node)
    {
        if(this._firstr_ != null)
        {
            this._firstr_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._firstr_ = node;
    }

    public PMethodSignature getBsm()
    {
        return this._bsm_;
    }

    public void setBsm(PMethodSignature node)
    {
        if(this._bsm_ != null)
        {
            this._bsm_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._bsm_ = node;
    }

    public TLParen getLParen()
    {
        return this._lParen_;
    }

    public void setLParen(TLParen node)
    {
        if(this._lParen_ != null)
        {
            this._lParen_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._lParen_ = node;
    }

    public PArgList getStaticargs()
    {
        return this._staticargs_;
    }

    public void setStaticargs(PArgList node)
    {
        if(this._staticargs_ != null)
        {
            this._staticargs_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._staticargs_ = node;
    }

    public TRParen getRParen()
    {
        return this._rParen_;
    }

    public void setRParen(TRParen node)
    {
        if(this._rParen_ != null)
        {
            this._rParen_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._rParen_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._dynamicinvoke_)
            + toString(this._stringConstant_)
            + toString(this._dynmethod_)
            + toString(this._firstl_)
            + toString(this._dynargs_)
            + toString(this._firstr_)
            + toString(this._bsm_)
            + toString(this._lParen_)
            + toString(this._staticargs_)
            + toString(this._rParen_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._dynamicinvoke_ == child)
        {
            this._dynamicinvoke_ = null;
            return;
        }

        if(this._stringConstant_ == child)
        {
            this._stringConstant_ = null;
            return;
        }

        if(this._dynmethod_ == child)
        {
            this._dynmethod_ = null;
            return;
        }

        if(this._firstl_ == child)
        {
            this._firstl_ = null;
            return;
        }

        if(this._dynargs_ == child)
        {
            this._dynargs_ = null;
            return;
        }

        if(this._firstr_ == child)
        {
            this._firstr_ = null;
            return;
        }

        if(this._bsm_ == child)
        {
            this._bsm_ = null;
            return;
        }

        if(this._lParen_ == child)
        {
            this._lParen_ = null;
            return;
        }

        if(this._staticargs_ == child)
        {
            this._staticargs_ = null;
            return;
        }

        if(this._rParen_ == child)
        {
            this._rParen_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._dynamicinvoke_ == oldChild)
        {
            setDynamicinvoke((TDynamicinvoke) newChild);
            return;
        }

        if(this._stringConstant_ == oldChild)
        {
            setStringConstant((TStringConstant) newChild);
            return;
        }

        if(this._dynmethod_ == oldChild)
        {
            setDynmethod((PUnnamedMethodSignature) newChild);
            return;
        }

        if(this._firstl_ == oldChild)
        {
            setFirstl((TLParen) newChild);
            return;
        }

        if(this._dynargs_ == oldChild)
        {
            setDynargs((PArgList) newChild);
            return;
        }

        if(this._firstr_ == oldChild)
        {
            setFirstr((TRParen) newChild);
            return;
        }

        if(this._bsm_ == oldChild)
        {
            setBsm((PMethodSignature) newChild);
            return;
        }

        if(this._lParen_ == oldChild)
        {
            setLParen((TLParen) newChild);
            return;
        }

        if(this._staticargs_ == oldChild)
        {
            setStaticargs((PArgList) newChild);
            return;
        }

        if(this._rParen_ == oldChild)
        {
            setRParen((TRParen) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
