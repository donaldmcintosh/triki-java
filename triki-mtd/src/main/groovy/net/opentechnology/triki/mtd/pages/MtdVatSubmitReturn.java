package net.opentechnology.triki.mtd.pages;

import org.apache.wicket.request.mapper.parameter.PageParameters;

public class MtdVatSubmitReturn extends MtdVatManage {

  public MtdVatSubmitReturn(PageParameters parameters) {
    super(parameters);
  }

  @Override
  protected void onConfigure() {
    super.onConfigure();
    setActiveMenu("returns");
  }
}
