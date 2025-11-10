import com.example.config.SparkSessionProvider
import com.example.listener.SparkListenerManager
import com.example.service.{EmployeeDataService, TracingService}
import org.apache.spark.sql.SparkSession
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll
import com.example.DataFrameMetrics

class JobIntegrationSpec extends AnyWordSpec with Matchers with BeforeAndAfterAll {

  private var spark: SparkSession = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    spark = SparkSessionProvider.get
    spark.sparkContext.setLogLevel("ERROR")
  }

  override def afterAll(): Unit = {
    SparkSessionProvider.stop()
    super.afterAll()
  }

  "Job workflow" when {

    "processing employee dataframe with tracing" should {

      "complete full workflow without exceptions" in {
        val tracingService = new TracingService("Job")
        val dataService = new EmployeeDataService(spark)
        val listenerManager = new SparkListenerManager(spark)

        noException should be thrownBy {
          tracingService.traceOperation("spark-dataframe-processing") { span =>
            listenerManager.registerJobListener(span)
            val employeesDF = dataService.createEmployeeDataFrame()
            val metrics = DataFrameMetrics.calculate(employeesDF)
            tracingService.recordMetrics(span, metrics)
          }
        }
      }
    }
  }
}