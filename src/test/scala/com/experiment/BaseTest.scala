package com.experiment

import org.scalatest._

trait BaseTest
  extends FlatSpec
    with OptionValues
    with Matchers
    with EitherValues
    with BeforeAndAfterEach
    with BeforeAndAfterAll
