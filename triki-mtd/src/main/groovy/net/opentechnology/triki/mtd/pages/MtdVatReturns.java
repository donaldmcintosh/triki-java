package net.opentechnology.triki.mtd.pages;

import org.apache.wicket.request.mapper.parameter.PageParameters;

public class MtdVatReturns extends MtdVatManage {

  public MtdVatReturns(PageParameters parameters) {
    super(parameters);
  }

  @Override
  protected void onConfigure() {
    super.onConfigure();
    setActiveMenu("returns");
  }
}
