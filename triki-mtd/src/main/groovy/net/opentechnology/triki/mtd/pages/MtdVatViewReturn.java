package net.opentechnology.triki.mtd.pages;

import net.opentechnology.triki.auth.components.FeedbackStringContainer;
import net.opentechnology.triki.auth.resources.SessionUtils;
import net.opentechnology.triki.mtd.security.HmrcIdentityProvider;
import net.opentechnology.triki.mtd.vatapi.client.HmrcClientUtils;
import net.opentechnology.triki.mtd.vatapi.client.HmrcVatClient;
import net.opentechnology.triki.mtd.vatapi.dto.VatObligations;
import net.opentechnology.triki.mtd.vatapi.dto.VatReturn;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import retrofit2.Call;
import retrofit2.Response;

public class MtdVatViewReturn extends MtdVatManage {

  private static final Logger logger = Logger.getLogger(MtdVatObligations.class);
  public static final String PERIOD_KEY = "periodKey";

  @SpringBean
  private SessionUtils sessionUtils;

  @SpringBean
  private HmrcClientUtils hmrcClientUtils;

  private FeedbackStringContainer viewReturnFeedback;

  private final String periodKey;

  public MtdVatViewReturn(PageParameters parameters) {
    super(parameters);
    periodKey = parameters.get(PERIOD_KEY).toString();

  }

  @Override
  protected void onConfigure() {
    super.onConfigure();
    setActiveMenu("returns");

    // Validation
    String accessToken = sessionUtils.getModuleToken(HmrcIdentityProvider.HMRC_TOKEN);
    String vrn = getVrn();

    try {
      HmrcVatClient hmrcVatClient = hmrcClientUtils.buildAuthBearerServiceClient(accessToken);
      Call<VatReturn> callable = hmrcVatClient.viewReturn(vrn, periodKey);

      Response<VatReturn> vatReturnResponse = callable.execute();

      viewReturnFeedback = new FeedbackStringContainer("viewReturnFeedback");
      add(viewReturnFeedback);

      add(new Label("vrn", vrn));
      add(new Label("periodKey", periodKey));

      if(vatReturnResponse.code() == 200) {
        VatReturn vatReturn = vatReturnResponse.body();
        add(new Label("vatDueSales", new PropertyModel(vatReturn, "vatDueSales")));
        add(new Label("vatDueAcquisitions", new PropertyModel(vatReturn, "vatDueAcquisitions")));
        add(new Label("totalVatDue", new PropertyModel(vatReturn, "totalVatDue")));
        add(new Label("vatReclaimedCurrPeriod", new PropertyModel(vatReturn, "vatReclaimedCurrPeriod")));
        add(new Label("netVatDue", new PropertyModel(vatReturn, "netVatDue")));
        add(new Label("totalValueSalesExVAT", new PropertyModel(vatReturn, "totalValueSalesExVAT")));
        add(new Label("totalValuePurchasesExVAT", new PropertyModel(vatReturn, "totalValuePurchasesExVAT")));
        add(new Label("totalValueGoodsSuppliedExVAT", new PropertyModel(vatReturn, "totalValueGoodsSuppliedExVAT")));
        add(new Label("totalAcquisitionsExVAT", new PropertyModel(vatReturn, "totalAcquisitionsExVAT")));
      }

    } catch (Exception e) {
      logger.error("Problems calling HMRC", e);
      viewReturnFeedback.setMsg(getResourceBundleMsg("problems"));
    }
  }
}
