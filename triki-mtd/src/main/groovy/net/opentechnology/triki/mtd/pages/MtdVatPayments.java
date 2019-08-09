package net.opentechnology.triki.mtd.pages;

import org.apache.wicket.request.mapper.parameter.PageParameters;

public class MtdVatPayments extends MtdVatManage {

  public MtdVatPayments(PageParameters parameters) {
    super(parameters);
  }

  @Override
  protected void onConfigure() {
    super.onConfigure();
    setActiveMenu("payments");
  }
}
