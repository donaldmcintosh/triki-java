package net.opentechnology.triki.mtd.pages;

import org.apache.wicket.request.mapper.parameter.PageParameters;

public class MtdVatViewReturn extends MtdVatManage {

  public MtdVatViewReturn(PageParameters parameters) {
    super(parameters);
  }

  @Override
  protected void onConfigure() {
    super.onConfigure();
    setActiveMenu("returns");
  }
}
