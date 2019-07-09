package net.opentechnology.triki.mtd.pages;

public class MtdVatObligations extends MtdVatManage {

  public MtdVatObligations() {
  }

  @Override
  protected void onConfigure() {
    super.onConfigure();
    setActiveMenu("obligations");
  }
}
